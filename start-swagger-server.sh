#!/bin/bash

# Simple script to start a local server for Swagger UI testing

PORT=8080
SWAGGER_DIR="src/test/resources"

echo "🚀 Starting Swagger UI Server..."
echo "📍 Port: $PORT"
echo "📁 Serving from: $SWAGGER_DIR"
echo ""

# Check if Python 3 is available
if command -v python3 &> /dev/null; then
    echo "✅ Using Python 3 server"
    echo "🌐 Swagger UI: http://localhost:$PORT/swagger-ui/index.html"
    echo "📄 API Docs: http://localhost:$PORT/openapi.json"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "----------------------------------------"
    
    cd "$(dirname "$0")" || exit 1
    python3 -m http.server $PORT --directory "$SWAGGER_DIR" --bind localhost
    
elif command -v python &> /dev/null; then
    echo "✅ Using Python 2 server"
    echo "🌐 Swagger UI: http://localhost:$PORT/swagger-ui/index.html"
    echo "📄 API Docs: http://localhost:$PORT/openapi.json"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "----------------------------------------"
    
    cd "$(dirname "$0")" || exit 1
    cd "$SWAGGER_DIR" || exit 1
    python -m SimpleHTTPServer $PORT
    
elif command -v npx &> /dev/null; then
    echo "✅ Using Node.js server"
    echo "🌐 Swagger UI: http://localhost:$PORT/swagger-ui/index.html"
    echo "📄 API Docs: http://localhost:$PORT/openapi.json"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "----------------------------------------"
    
    cd "$(dirname "$0")" || exit 1
    npx serve "$SWAGGER_DIR" -p $PORT -s
    
else
    echo "❌ No suitable server found!"
    echo "Please install one of the following:"
    echo "  - Python 3: brew install python3"
    echo "  - Node.js: brew install node"
    exit 1
fi