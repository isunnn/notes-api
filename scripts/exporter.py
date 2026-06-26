import http.server, socketserver, os, urllib.request, base64, re, threading, time

METRICS_FILE = "/tmp/coverage.prom"

def update_metrics():
    while True:
        try:
            key = os.environ.get("SONAR_PROJECT_KEY", "")
            token = os.environ.get("SONAR_TOKEN", "")
            url = f"https://sonarcloud.io/api/measures/component?component={key}&metricKeys=coverage"
            req = urllib.request.Request(url)
            creds = base64.b64encode(f"{token}:".encode()).decode()
            req.add_header("Authorization", f"Basic {creds}")
            resp = urllib.request.urlopen(req, timeout=15)
            data = resp.read().decode()
            match = re.search(r'"value":"([^"]+)"', data)
            coverage = match.group(1) if match else "0"
        except Exception:
            coverage = "0"
        with open(METRICS_FILE, "w") as f:
            f.write("# HELP sonarcloud_test_coverage Test coverage percentage\n")
            f.write("# TYPE sonarcloud_test_coverage gauge\n")
            f.write(f"sonarcloud_test_coverage {coverage}\n")
        time.sleep(60)

class Handler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        try:
            with open(METRICS_FILE, "r") as f:
                body = f.read()
        except FileNotFoundError:
            body = "# HELP sonarcloud_test_coverage Test coverage percentage\n# TYPE sonarcloud_test_coverage gauge\nsonarcloud_test_coverage 0\n"
        self.send_response(200)
        self.send_header("Content-Type", "text/plain; version=0.0.4")
        self.end_headers()
        self.wfile.write(body.encode())
    def log_message(self, format, *args):
        pass

threading.Thread(target=update_metrics, daemon=True).start()
with socketserver.TCPServer(("", 9100), Handler) as httpd:
    httpd.serve_forever()
