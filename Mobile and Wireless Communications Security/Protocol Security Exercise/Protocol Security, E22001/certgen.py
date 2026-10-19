import argparse
import datetime
import os
import sys

try:
    from cryptography import x509
    from cryptography.x509.oid import NameOID
    from cryptography.hazmat.primitives import hashes, serialization
    from cryptography.hazmat.primitives.asymmetric import rsa
    from cryptography.hazmat.backends import default_backend
except ImportError:
    print("[ERROR] Missing dependency. Run:  pip install cryptography")
    sys.exit(1)


# Certificate Generation 
def generate_private_key(key_size: int = 2048):
    """Generate an RSA private key."""
    return rsa.generate_private_key(
        public_exponent=65537,
        key_size=key_size,
        backend=default_backend()
    )


def generate_certificate(key, cn: str, org: str, country: str, days: int):
    """Build and sign a self-signed X.509 certificate."""
    subject = issuer = x509.Name([
        x509.NameAttribute(NameOID.COUNTRY_NAME,             country),
        x509.NameAttribute(NameOID.ORGANIZATION_NAME,        org),
        x509.NameAttribute(NameOID.COMMON_NAME,              cn),
    ])

    now = datetime.datetime.utcnow()

    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + datetime.timedelta(days=days))
        # Mark this as a CA cert so it can sign itself
        .add_extension(
            x509.BasicConstraints(ca=True, path_length=None),
            critical=True
        )
        # Subject Alternative Name — required by modern browsers/clients
        .add_extension(
            x509.SubjectAlternativeName([x509.DNSName(cn)]),
            critical=False
        )
        .sign(key, hashes.SHA256(), default_backend())
    )
    return cert


# File I/O 
def save_pem_files(key, cert, output_dir: str):
    os.makedirs(output_dir, exist_ok=True)

    key_path  = os.path.join(output_dir, "key.pem")
    cert_path = os.path.join(output_dir, "cert.pem")

    # Private key,  no password for simplicity (add one in production!)
    with open(key_path, "wb") as f:
        f.write(key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption()
        ))

    # Certificate
    with open(cert_path, "wb") as f:
        f.write(cert.public_bytes(serialization.Encoding.PEM))

    return key_path, cert_path

# CLI 
def parse_args():
    parser = argparse.ArgumentParser(
        prog="certgen",
        description="Generate a self-signed TLS certificate and private key in PEM format.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )
    parser.add_argument("--cn",      default="localhost",     help="Common Name (domain)")
    parser.add_argument("--org",     default="MyOrganization",help="Organization name")
    parser.add_argument("--country", default="US",            help="2-letter country code")
    parser.add_argument("--days",    default=365, type=int,   help="Certificate validity in days")
    parser.add_argument("--keysize", default=2048, type=int,
                        choices=[2048, 4096],                 help="RSA key size in bits")
    parser.add_argument("--out",     default="certificates",  help="Output directory")
    return parser.parse_args()


def print_cert_info(cert, key_path: str, cert_path: str):
    """Print a human-readable summary of the generated certificate."""
    print("\n" + "-" * 50)
    print("  Certificate Generated Successfully")
    print("-" * 50)
    print(f"  Subject  : {cert.subject.rfc4514_string()}")
    print(f"  Serial   : {cert.serial_number}")
    print(f"  Valid    : {cert.not_valid_before_utc.date()} → {cert.not_valid_after_utc.date()}")
    print(f"  Key file : {key_path}")
    print(f"  Cert file: {cert_path}")
    print("-" * 50 + "\n")


# Entry Point 
def main():
    args = parse_args()

    print(f"\n Generating {args.keysize}-bit RSA key...")
    key = generate_private_key(args.keysize)

    print(f" Building self-signed certificate for '{args.cn}' ({args.days} days)...")
    cert = generate_certificate(key, args.cn, args.org, args.country, args.days)

    print(f" Saving PEM files to '{args.out}/'...")
    key_path, cert_path = save_pem_files(key, cert, args.out)

    print_cert_info(cert, key_path, cert_path)
    print("Done!")


if __name__ == "__main__":
    main()