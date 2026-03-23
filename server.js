const express = require('express');
const cors = require('cors');
const { spawn } = require('child_process');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

function executeJava(className, inputCode) {
    return new Promise((resolve, reject) => {
        // Spawn the Java process
        const javaProcess = spawn('java', [className], {
            cwd: path.join(__dirname, 'bin')
        });

        let output = '';
        let errorOutput = '';

        javaProcess.stdout.on('data', (data) => {
            output += data.toString();
        });

        javaProcess.stderr.on('data', (data) => {
            errorOutput += data.toString();
        });

        javaProcess.on('close', (code) => {
            if (code !== 0 || errorOutput) {
                // Return what we have, Java might have printed stack traces to stdout or stderr
                resolve({ success: false, output: output + '\n' + errorOutput });
            } else {
                resolve({ success: true, output });
            }
        });

        javaProcess.on('error', (err) => {
            resolve({ success: false, output: `Failed to start process: ${err.message}` });
        });

        // Write the input code and the END marker to stdin
        javaProcess.stdin.write(inputCode);
        if (!inputCode.endsWith('\n')) {
            javaProcess.stdin.write('\n');
        }
        javaProcess.stdin.write('END\n');
        javaProcess.stdin.end();
    });
}

// Ensure the code input is clean
const prepareCode = (code) => {
    return code || '';
};

app.post('/api/compile', async (req, res) => {
    const { code } = req.body;
    if (code === undefined) {
        return res.status(400).json({ error: 'Code is required' });
    }

    try {
        const result = await executeJava('MiniCompiler', prepareCode(code));
        
        // The Java program outputs a banner: "Enter your program (end with line: END):\n\n--- VM OUTPUT ---"
        // Let's clean the output for the frontend.
        let cleanOutput = result.output;
        const bannerIndex = cleanOutput.indexOf('--- VM OUTPUT ---');
        if (bannerIndex !== -1) {
            cleanOutput = cleanOutput.substring(bannerIndex + '--- VM OUTPUT ---'.length).trim();
        }
        
        res.json({ ...result, output: cleanOutput });
    } catch (err) {
        res.status(500).json({ success: false, output: err.message });
    }
});

app.post('/api/translate', async (req, res) => {
    const { code } = req.body;
    if (code === undefined) {
        return res.status(400).json({ error: 'Code is required' });
    }

    try {
        const result = await executeJava('VMToMipsTranslator', prepareCode(code));
        
        // Clean banner: "Paste VM code (end with line: END):\n\n--- MIPS OUTPUT ---"
        let cleanOutput = result.output;
        const bannerIndex = cleanOutput.indexOf('--- MIPS OUTPUT ---');
        if (bannerIndex !== -1) {
            cleanOutput = cleanOutput.substring(bannerIndex + '--- MIPS OUTPUT ---'.length).trim();
        }

        res.json({ ...result, output: cleanOutput });
    } catch (err) {
        res.status(500).json({ success: false, output: err.message });
    }
});

app.listen(PORT, () => {
    console.log(`Server is running at http://localhost:${PORT}`);
});
