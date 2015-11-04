package eureka.main;

import java.util.Hashtable;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import eureka.base.WorkingMemory;
import eureka.base.exceptions.InvalidOperatorException;
import eureka.engine.BackwardChaining;
import eureka.engine.Engine;
import eureka.engine.ForwardChaining;
import eureka.util.HibernateUtil;

public class Main {

	public static Engine forwardTest( WorkingMemory wm ) {
		ForwardChaining f = new ForwardChaining();

		Hashtable<Object, Object> params = new Hashtable<Object, Object>();

		f.init(wm, params,"");
                

		return f;
	}

	public static Engine backwardTest( WorkingMemory wm, String variable, String value ) {
		BackwardChaining b = new BackwardChaining();

		Hashtable<Object, Object> params = new Hashtable<Object, Object>();
		if(variable != null) {
			params.put(BackwardChaining.GOAL_VARIABLE, variable);
		}
		if(value != null) {
			params.put(BackwardChaining.GOAL_VALUE, value);
		}

		b.init(wm, params,variable);

		return b;
	}

	public static void main(String[] args) throws NumberFormatException,
			InvalidOperatorException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                HibernateUtil.beginTransaction();

                WorkingMemory wm = new WorkingMemory();
                //wm.addAllVariables(FactsParser.loadFacts("rsc//carros_facts.txt"));
                
                Engine e = backwardTest(wm, "devo_ir_a_praia", null);
               // Engine e = forwardTest(wm);
               //Engine e = backwardTest(wm, "Veiculo", null);
                 //Engine e = backwardTest(wm, "asma induzida por v�rus", null);
                e.execute();
                wm.printState();
                
	}
}
