from http.server import HTTPServer, SimpleHTTPRequestHandler
import ssl
import os

port = 4000

def run_https_server(cert_dir, server_address=('localhost', port)):
    cert_file = os.path.join(cert_dir, 'cert.pem')
    key_file  = os.path.join(cert_dir, 'key.pem')

    httpd = HTTPServer(server_address, SimpleHTTPRequestHandler)

    # Compatible with Python 3.12+ (ssl.wrap_socket was removed in 3.12)
    ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    ctx.load_cert_chain(certfile=cert_file, keyfile=key_file)
    httpd.socket = ctx.wrap_socket(httpd.socket, server_side=True)

    print(f"Starting HTTPS server at https://{server_address[0]}:{server_address[1]}/")
    httpd.serve_forever()

cert_folder = './certificates'
run_https_server(cert_folder)