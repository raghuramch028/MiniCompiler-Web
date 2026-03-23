# Start with a lightweight Linux image running Node.js
FROM node:18-bullseye-slim

# Install Java JDK (required to run your compiler)
RUN apt-get update && \
    apt-get install -y default-jdk && \
    apt-get clean;

# Create and set the working directory
WORKDIR /app

# Copy package files and install the node_modules
COPY package*.json ./
RUN npm install

# Copy all the rest of your project files
COPY . .

# Ensure the Java code is compiled into the bin/ folder within the container
RUN mkdir -p bin && javac -d bin src/MiniCompiler.java src/VMToMipsTranslator.java

# Expose the exact port your server uses
EXPOSE 3000

# The command to start the web server
CMD ["npm", "start"]
