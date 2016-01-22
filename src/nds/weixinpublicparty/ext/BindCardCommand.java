package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;

public class BindCardCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		
		
		return vh;
	}

}
