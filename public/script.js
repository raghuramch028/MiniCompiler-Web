document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const compileTabBtn = document.querySelector('[data-tab="compile-tab"]');
    const translateTabBtn = document.querySelector('[data-tab="translate-tab"]');
    const compileTab = document.getElementById('compile-tab');
    const translateTab = document.getElementById('translate-tab');
    
    const compileInput = document.getElementById('compile-input');
    const compileOutput = document.getElementById('compile-output');
    const btnCompile = document.getElementById('btn-compile');

    const translateInput = document.getElementById('translate-input');
    const translateOutput = document.getElementById('translate-output');
    const btnTranslate = document.getElementById('btn-translate');

    // Tab Switching Logic
    const switchTab = (activeBtn, inactiveBtn, activeContent, inactiveContent) => {
        activeBtn.classList.add('active');
        inactiveBtn.classList.remove('active');
        activeContent.classList.add('active');
        inactiveContent.classList.remove('active');
    };

    compileTabBtn.addEventListener('click', () => {
        switchTab(compileTabBtn, translateTabBtn, compileTab, translateTab);
    });

    translateTabBtn.addEventListener('click', () => {
        switchTab(translateTabBtn, compileTabBtn, translateTab, compileTab);
    });

    // Compile Logic
    btnCompile.addEventListener('click', async () => {
        const code = compileInput.value.trim();
        if (!code) {
            compileOutput.textContent = 'Please enter some source code.';
            compileOutput.classList.add('error');
            return;
        }

        // Updating UI
        btnCompile.classList.add('loading');
        btnCompile.querySelector('span').textContent = 'Compiling...';
        compileOutput.textContent = 'Compiling...';
        compileOutput.classList.remove('error');

        try {
            const response = await fetch('/api/compile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code })
            });

            const result = await response.json();

            if (result.success) {
                compileOutput.textContent = result.output || 'No output generated.';
                // Auto-fill the translate tab
                translateInput.value = result.output;
            } else {
                compileOutput.textContent = result.output || 'Compilation failed.';
                compileOutput.classList.add('error');
            }
        } catch (error) {
            compileOutput.textContent = `Error: ${error.message}`;
            compileOutput.classList.add('error');
        } finally {
            btnCompile.classList.remove('loading');
            btnCompile.querySelector('span').textContent = 'Compile to VM';
        }
    });

    // Translate Logic
    btnTranslate.addEventListener('click', async () => {
        const code = translateInput.value.trim();
        if (!code) {
            translateOutput.textContent = 'Please enter some VM code.';
            translateOutput.classList.add('error');
            return;
        }

        // Updating UI
        btnTranslate.classList.add('loading');
        btnTranslate.querySelector('span').textContent = 'Translating...';
        translateOutput.textContent = 'Translating...';
        translateOutput.classList.remove('error');

        try {
            const response = await fetch('/api/translate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code })
            });

            const result = await response.json();

            if (result.success) {
                translateOutput.textContent = result.output || 'No output generated.';
            } else {
                translateOutput.textContent = result.output || 'Translation failed.';
                translateOutput.classList.add('error');
            }
        } catch (error) {
            translateOutput.textContent = `Error: ${error.message}`;
            translateOutput.classList.add('error');
        } finally {
            btnTranslate.classList.remove('loading');
            btnTranslate.querySelector('span').textContent = 'Translate to MIPS';
        }
    });
});
