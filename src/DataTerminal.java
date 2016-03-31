import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.telephony.Address;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.Provider;
import javax.telephony.ProviderObserver;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.TerminalObserver;
import javax.telephony.events.ProvEv;
import javax.telephony.events.ProvInServiceEv;
import javax.telephony.events.TermEv;
import com.cisco.cti.util.Condition;
import com.cisco.jtapi.extensions.CiscoTermInServiceEv;
import com.cisco.jtapi.extensions.CiscoTermOutOfServiceEv;
import com.cisco.jtapi.extensions.CiscoTerminal;

public class DataTerminal implements ProviderObserver, TerminalObserver {
  public String phoneCmd;
  public String resp;
  private Address destAddress;
  private CiscoTerminal observedTerminal;
  Provider provider;
  Condition condInService;

  //Constructor for DataTerminal
  public DataTerminal(@SuppressWarnings("rawtypes") List jtapi) {
  	provider = (Provider) jtapi.get(0);
  	condInService = (Condition) jtapi.get(1);
  	try {
  	  provider.addObserver(this);
  	  condInService.waitTrue();
  	} catch (ResourceUnavailableException | MethodNotSupportedException e) {
  	  e.printStackTrace();
  	}
  }

  public void providerChangedEvent(ProvEv[] evtList) {
  	if(evtList != null) {
  	  for(int i = 0; i < evtList.length; i++) {
  	    if(evtList[i] instanceof ProvInServiceEv) {
    		  condInService.set();
    		}
  	  }
  	}
  }

  public void terminalChangedEvent(TermEv[] evts) {
	  for(int i=0; i<evts.length; i++) {
	  	  System.out.println(evts[i].getID());
  		  switch(evts[i].getID()) {
		      case CiscoTermInServiceEv.ID:
  			    try {
  				      resp = observedTerminal.sendData(phoneCmd);
  			    } catch (Exception e) {
  				    resp = e.toString();
  				    System.out.println(resp);
  			    }
  			    break;
		      case CiscoTermOutOfServiceEv.ID:
            break;
		  }
	  }
  }

  public String execAction(String[] params) throws UnsupportedEncodingException {
	  String devName = params[0];
	  phoneCmd = params[1];
	  try {
  		Terminal term = provider.getTerminal(devName);
  		destAddress = term.getAddresses()[0];
  		Terminal[] ct = destAddress.getTerminals();
  		for(int i=0;i<ct.length;i++) {
  			String cscoTerm = (CiscoTerminal) ct[i] + "";
  			System.out.println(cscoTerm);
  			if(cscoTerm.equals(devName)) {
  				System.out.println("Hello World");
  				observedTerminal = (CiscoTerminal) ct[i];
  				break;
  			} else {
  				continue;
  			}
  		}
  		if(observedTerminal != null) {
  			observedTerminal.addObserver(this);
  		}
  		return "completed";
	  } catch (Exception e) {
  		System.out.println(e);
  		return e.toString();
	  }
  }

  public String getResp() {
	  if(resp != null) {
		  return new String(resp);
	  } else {
		  return "no resp";
	  }
  }

  public void close() {
	provider.shutdown();
  }
}
