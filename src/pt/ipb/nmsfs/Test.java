/**
 * $Id$
@copyright@
@license@
 */

package pt.ipb.nmsfs;

import java.io.IOException;

import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Test {

	public static void main(String[] args) throws IOException {
		Address targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
		   TransportMapping transport = new DefaultUdpTransportMapping();
		   Snmp snmp = new Snmp(transport);
		   USM usm = new USM(SecurityProtocols.getInstance(),
		                     new OctetString(MPv3.createLocalEngineID()), 0);
		   SecurityModels.getInstance().addSecurityModel(usm);
		   transport.listen();

		// add user to the USM
		   snmp.getUSM().addUser(new OctetString("bart"),
		                         new UsmUser(new OctetString("bart"),
		                                     AuthMD5.ID,
		                                     new OctetString("bartsimpson"),
		                                     null,
		                                     null));
		   // create the target
		   UserTarget target = new UserTarget();
		   target.setAddress(targetAddress);
		   target.setRetries(1);
		   target.setTimeout(5000);
		   target.setVersion(SnmpConstants.version3);
		   target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		   target.setSecurityName(new OctetString("bart"));

		   // create the PDU
		   PDU pdu = new ScopedPDU();
		   pdu.add(new VariableBinding(new OID("1")));
		   pdu.setType(PDU.GETNEXT);

		   // send the PDU
		   ResponseEvent response = snmp.send(pdu, target);
		   // extract the response PDU (could be null if timed out)
		   PDU responsePDU = response.getResponse();
		   // extract the address used by the agent to send the response:
		   Address peerAddress = response.getPeerAddress();
System.out.println(responsePDU.toString());
	}
}
