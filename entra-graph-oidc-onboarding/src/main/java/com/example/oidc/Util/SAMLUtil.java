package com.example.oidc.Util;
package com.sc.mfa.control.adoption.v1.utils;

import com.microsoft.graph.models.*;

/**
 * This class is used for SAML supportive methods
 */
@Component
public class SamlUtil {

    @Autowired
    CommonUtil commonUtil;

    /**
     * This method is used to assign response signing certificate and passwordCredentials
     *
     * @param servicePrincipal to assign the response signing certificate and passwordCredentials
     * @param signCert          for response signing
     * @param verifyCert        for response verify
     * @param signCertPass      to access signing cert
     */
    public ServicePrincipal responseSigningCertificate(ServicePrincipal servicePrincipal, String signCert,
            String verifyCert, String signCertPass) {

        String uuid = UUID.randomUUID().toString();
        List<KeyCredential> keyCredentials = new LinkedList<>();

        keyCredentials.add(commonUtil.addKeyCredential(uuid, KeyType.X509CertAndPassword,
                KeyUsage.Sign, signCert));

        keyCredentials.add(commonUtil.addKeyCredential(UUID.randomUUID().toString(), KeyType.AsymmetricX509Cert,
                KeyUsage.Verify, verifyCert));

        LinkedList<PasswordCredential> passwordCredentials = new LinkedList<>();
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setKeyId(UUID.fromString(uuid));
        passwordCredential.setSecretText(signCertPass);
        passwordCredentials.add(passwordCredential);

        servicePrincipal.setKeyCredentials(keyCredentials);
        servicePrincipal.setPasswordCredentials(passwordCredentials);

        return servicePrincipal;
    }

    /**
     * This method is used to set saml request signing and Encrypt certificate
     *
     * @param application       to set saml request signing and Encrypt certificate
     * @param requestSignCert   request signing certificate
     * @param tokenEncryptCert  Encrypt certificate
     * @return application to process
     */
    public Application setRequestSigningEncryptCertificate(Application application, String requestSignCert,
            String tokenEncryptCert) {

        if (!requestSignCert.equals("null") && !requestSignCert.isEmpty() && !requestSignCert.equals("NA")) {
            RequestSignatureVerification requestSignatureVerification = new RequestSignatureVerification();
            requestSignatureVerification.setIsSignedRequestRequired(true);
            application.setRequestSignatureVerification(requestSignatureVerification);
        }

        String uuid = UUID.randomUUID().toString();

        if (!tokenEncryptCert.equals("null") && !tokenEncryptCert.isEmpty() && !tokenEncryptCert.equals("NA")) {
            application.setTokenEncryptionKeyId(UUID.fromString(uuid));
        }

        application.setKeyCredentials(setKeyCredentials(uuid, "", requestSignCert, tokenEncryptCert));
        return application;
    }

    /**
     * This method is used to set Key Credentials
     *
     * @param uuid          for each certificate
     * @param signCert      signing certificate
     * @param verifyCert    verify certificate
     * @param encryptCert   encrypt certificate
     * @return Key Credentials to process
     */
    public List<KeyCredential> setKeyCredentials(String uuid, String signCert, String verifyCert,
            String encryptCert) {

        List<KeyCredential> keyCredentials = new LinkedList<>();

        if (!signCert.equals("null") && !signCert.isEmpty() && !signCert.equals("NA")) {
            keyCredentials.add(commonUtil.addKeyCredential(uuid, KeyType.X509CertAndPassword,
                    KeyUsage.Sign, signCert));
        }

        if (!verifyCert.equals("null") && !verifyCert.isEmpty() && !verifyCert.equals("NA")) {
            keyCredentials.add(commonUtil.addKeyCredential(UUID.randomUUID().toString(), KeyType.AsymmetricX509Cert,
                    KeyUsage.Verify, verifyCert));
        }

        if (!encryptCert.equals("null") && !encryptCert.isEmpty() && !encryptCert.equals("NA")) {
            keyCredentials.add(commonUtil.addKeyCredential(uuid, KeyType.AsymmetricX509Cert,
                    KeyUsage.Encrypt, encryptCert));
        }

        return keyCredentials;
    }
}