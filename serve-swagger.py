#!/usr/bin/env python3
"""
Simple HTTP server to serve the Swagger UI for testing purposes.
This serves the swagger-ui from src/test/resources/swagger-ui/
"""

import http.server
import socketserver
import os
import sys
from pathlib import Path

# Set the port
PORT = 8080

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        # Set the directory to serve from
        swagger_dir = Path("src/test/resources")
        super().__init__(*args, directory=swagger_dir, **kwargs)
    
    def end_headers(self):
        # Add CORS headers to allow cross-origin requests
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')
        super().end_headers()
    
    def do_GET(self):
        # Redirect root to swagger-ui
        if self.path == '/':
            self.send_response(302)
            self.send_header('Location', '/swagger-ui/index.html')
            self.end_headers()
            return
        
        # Handle swagger-ui path
        if self.path == '/swagger-ui' or self.path == '/swagger-ui/':
            self.send_response(302)
            self.send_header('Location', '/swagger-ui/index.html')
            self.end_headers()
            return
            
        super().do_GET()

def main():
    # Change to the project directory
    script_dir = Path(__file__).parent
    os.chdir(script_dir)
    
    print(f"Starting server on http://localhost:{PORT}")
    print(f"Swagger UI will be available at: http://localhost:{PORT}/swagger-ui/index.html")
    print("Press Ctrl+C to stop the server")
    
    try:
        with socketserver.TCPServer(("", PORT), CustomHTTPRequestHandler) as httpd:
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")
    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"Port {PORT} is already in use. Try a different port or stop the existing process.")
            sys.exit(1)
        else:
            raise

if __name__ == "__main__":
    main()