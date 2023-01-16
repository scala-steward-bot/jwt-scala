package io.really.jwt

import java.security.spec.PKCS8EncodedKeySpec
import java.security.PrivateKey
import java.security.PublicKey
import io.really.jwt.JWTException.InvalidPublicKey
import io.really.jwt.JWTException.InvalidPrivateKey
import org.apache.commons.codec.binary.Base64
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object PemUtil {

  def decodePublicKey(pem: String): PublicKey = {
    val trial = Try {
      val bytes = pemToDer(pem)
      DerUtil.decodePublicKey(bytes)
    }
    trial.getOrElse(throw new InvalidPublicKey())
  }

  def decodePrivateKey(pem: String): PrivateKey = {
    val trial = Try {
      val bytes = pemToDer(pem)
      DerUtil.decodePrivateKey(bytes)
    }
    trial.getOrElse(throw new InvalidPrivateKey())
  }

  def isPublicKey(pem: String): Boolean = {
    Try {
      val bytes = pemToDer(pem)
      DerUtil.decodePublicKey(bytes)
    } match {
      case Success(v) => true
      case Failure(e) => false
    }
  }

  def removeBeginEnd(pem: String) = {
    pem
      .replaceAll("-----BEGIN (.*)-----", "")
      .replaceAll("-----END (.*)----", "")
      .replaceAll("\r\n", "")
      .replaceAll("\n", "")
      .trim()
  }

  private def pemToDer(pem: String) = {
    val removedpem = removeBeginEnd(pem)
    Base64.decodeBase64(removedpem)
  }
}

object DerUtil {

  import java.security.spec.X509EncodedKeySpec
  import java.security.KeyFactory
  import java.security.Security
  import org.bouncycastle.jce.provider.BouncyCastleProvider

  if (Security.getProvider("BC") == null) Security.addProvider(new BouncyCastleProvider())

  def decodePublicKey(der: Array[Byte]): PublicKey = {
    val spec = new X509EncodedKeySpec(der)
    val kf = KeyFactory.getInstance("RSA", "BC")
    kf.generatePublic(spec)
  }

  def decodePrivateKey(der: Array[Byte]): PrivateKey = {
    val spec = new PKCS8EncodedKeySpec(der)
    val kf = KeyFactory.getInstance("RSA", "BC")
    kf.generatePrivate(spec);
  }

}
