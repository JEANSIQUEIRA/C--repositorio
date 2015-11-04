package eureka.engine;

import br.com.expert.messaging.PerguntaMessaging;
import br.com.expert.model.ItemRegra;
import br.com.expert.model.Mensagem;
import br.com.expert.model.Regra;
import br.com.expert.model.Resposta;
import br.com.expert.model.Variavel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import eureka.base.BooleanClause;
import eureka.base.Clause;
import eureka.base.EOperator;
import eureka.base.Rule;
import eureka.base.RuleBase;
import eureka.base.RuleVariable;
import eureka.base.WorkingMemory;
import eureka.base.exceptions.InvalidOperatorException;
import eureka.dao.FacadeDAO;
import eureka.dao.RuleDAO;
import eureka.util.HibernateUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Algoritmo de inferncia Backward chaining, dispara as regras certas a fim de
 * encontrar o valor de uma varivel
 */
public class BackwardChaining implements Engine, Serializable
{

    public EntityManager em;
    public static final String GOAL_VARIABLE = "GOAL_VARIABLE";
    public static final String GOAL_VALUE = "GOAL_VALUE";
    private static final String INFO_REQUEST = Messages.getString("BackwardChaining.INFO_REQUEST");
    private static final String INFO_REQUEST_TITLE = Messages.getString("BackwardChaining.INFO_REQUEST_TITLE");
    private WorkingMemory wm;
    private RuleDAO rb;
    private Hashtable<Object, Object> params;
    private boolean executando = false;
    private Stack<Rule> pilhaRegras = new Stack<Rule>();
    private Stack<Rule> guardaVolume = new Stack<Rule>();
    Clause clausulaAtual;
    String varivelObjetivo;
    int posicao = 0;
    Map<String, String> processado = new HashMap<String, String>();

    /**
     * Retorna as Regras que atribui o valor <i>value</i> a variavel
     * <i>variable</i> ou s as regras que possui <i>variable</i> no consequente
     * caso <i>valu</i> seja null
     */
    private List<Rule> goalRules(String variable, String value)
    {

        rb = FacadeDAO.getFacadeDAO().getRuleDAO();

        List<Rule> goalRules = new ArrayList<Rule>();
        for (Rule rule : rb.findAll())
        {
            if (rule.getConsequent().getVariableLabel().equals(variable))
            {
                if (value != null)
                {
                    if (rule.getConsequent().getTargetValue().equals(value))
                    {
                        goalRules.add(rule);
                    }
                }
                else
                {
                    goalRules.add(rule);
                }
            }
        }
        return goalRules;
    }

    /**
     * inicializa o algoritmo adicionando WorkingMemory os fatos no arquivo
     * factsPath(ou no faz nada se factsPath for null)<br>
     * e Adiciona as regras do arquivo rulesPath(ou no faz nada se rulesPath for
     * null)
     */
    @Override
    public void init(WorkingMemory wm,
            Hashtable<Object, Object> params,
            String varivelObjetivo)
    {
        this.params = params;
        this.wm = wm;
        this.varivelObjetivo = varivelObjetivo;
    }

    /**
     * Executa o algoritmo tentando verificar se o valor da <i>varivel</i>
     * <i>value<i><br>
     * ou qual o valor da varivel caso <i>value</i> seja null
     */
    @Override
    public void execute()
    {
        HibernateUtil.beginTransaction();
        executando = true;
        execute((String) params.get(GOAL_VARIABLE),
                (String) params.get(GOAL_VALUE));
        HibernateUtil.commit();
    }

    /* public void primeiraPergunta(String objetivo, String valor)
     {
     procurarEntao(objetivo, valor);
     }

     public void procurarEntao(String variable, String value) {

     List<Rule> goalRules = goalRules(variable, value);//Recupera todas as variaveis que possuam o valor passado como consequente

     pilhaRegras.addAll(goalRules);//empilha as regras em uma pilha

     if (!pilhaRegras.isEmpty()) {
     wm.referTo(variable);
     }
     if (!pilhaRegras.isEmpty() && wm.getVariable(variable) == null)//verifica se ainda possui alguma regra a ser processada
     {
            
     obterProximaPergunta();
            
     } else {
     System.out.println("Este sistema especilista já foi processado");
     }
     }

     public void obterProximaPergunta() {
     Rule rule = pilhaRegras.peek();//obtem a proxima regra
     Clause clausula = null;
     for (Clause c : rule.getAntecedent()) {//busca os "itens da regra" ou seja as clausulas da regra
     if (rule.getTruth() == null) {
     c.check(wm);//verifica se a clausula já foi processada
     if (c.getTruth() == null) {//se null, o usuário precisa responder a pergunta, entao a clausula é adicionada a uma lista de clausulas indefinidas
     clausula = c;
     break;
     }
     }
     }
     if(clausula != null)
     {
     guardaVolume.push(rule);
     procurarEntao(clausula.getVariableLabel(), null);
            
     }
     }*/
    private void execute(String variable, String value)
    {

        if (executando)
        {

            // pilha de regras que alteram a varivel objetivo
            Stack<Rule> goalStack = new Stack<Rule>();

            List<Rule> goalRules = goalRules(variable, value);//Recupera todas as variaveis que possuam o valor passado como consequente

            goalStack.addAll(goalRules);//empilha as regras em uma pilha

            if (!goalStack.isEmpty())
            {
                wm.referTo(variable);
            }

            while (!goalStack.isEmpty() && wm.getVariable(variable) == null && executando)
            {

                Rule rule = goalStack.peek();
                /*if(!processado.containsKey(rule.getLabel()))
                 {
                 posicao ++;
                 processado.put(rule.getLabel(), null);
                 }*/
                //enviaNotificacaoDepuracao("REGRA", rule.getLabel(),""+posicao);
                List<Clause> undefinedClauses = new ArrayList<Clause>();

                try
                {
                    for (Clause c : rule.getAntecedent())
                    {//busca os "itens da regra" ou seja as clausulas da regra
                        if (rule.getTruth() == null)
                        {
                            c.check(wm);//verifica se a clausula já foi processada
                            if (c.getTruth() == null)
                            {//se null, o usuário precisa responder a pergunta, entao a clausula é adicionada a uma lista de clausulas indefinidas
                                undefinedClauses.add(c);
                            }
                            else
                            {
                                PerguntaMessaging pergunta = new PerguntaMessaging();
                                Mensagem mensagem = new Mensagem();
                                mensagem.setTipo("RESPOSTA");
                                mensagem.setObjeto("" + c.getTruth());
                                mensagem.setTipoAvisoDepuracao("ITEM");
                                mensagem.setPosicao("" + ((BooleanClause) c).getIditemregra());
                                pergunta.executeAskNotification(mensagem);
                                System.out.println("valor da variavel:"+c.getVariableLabel()+"  foi:" + c.getTruth());
                            }
                        }
                        else
                        {
                            PerguntaMessaging pergunta = new PerguntaMessaging();
                            Mensagem mensagem = new Mensagem();
                            mensagem.setTipo("RESPOSTA");
                            mensagem.setObjeto("" + rule.getTruth());
                            mensagem.setTipoAvisoDepuracao("REGRA");
                            mensagem.setPosicao("" + rule.getLabel());
                            pergunta.executeAskNotification(mensagem);
                            System.out.println("valor da variavel:"+c.getVariableLabel()+"  foi:" + c.getTruth());
                        }
                    }

                    for (Clause c : undefinedClauses)
                    {//percorre as lista de clausulas(perguntas) a serem respondias pelos usuários
                        if (executando)
                        {
                            rule.update();
                            if (!processado.containsKey("" + ((BooleanClause) c).getIditemregra()))
                            {
                                posicao++;
                                processado.put("" + ((BooleanClause) c).getIditemregra(), null);
                            }
                            //enviaNotificacaoDepuracao("ITEM", ""+((BooleanClause)c).getIditemregra(),""+posicao);
                            if (rule.getTruth() == null)
                            {//se a regra ainda nao foi processada, começa a processar cada clausula

                                execute(c.getVariableLabel(), null);//chama recursivamente a funcao execute(), para que a variável da clausula seja processada

                                c.check(wm);

                                if (c.getTruth() == null)
                                {

                                    // se nao houver regras referindo-se a essa variavel no consequente
                                    if (!wm.wasReferred(c.getVariableLabel()))
                                    {

                                        clausulaAtual = c;
                                        executando = false;
                                        // pergunta ao usuario se a clausula e verdadeira
                                        fazPergutaParaUsuario(c);
                                        break;

                                    }
                                }
                                else
                                {
                                    PerguntaMessaging pergunta = new PerguntaMessaging();
                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setTipo("RESPOSTA");
                                    mensagem.setObjeto("" + c.getTruth());
                                    mensagem.setTipoAvisoDepuracao("ITEM");
                                    mensagem.setPosicao("" + ((BooleanClause) c).getIditemregra());
                                    pergunta.executeAskNotification(mensagem);
                                    System.out.println("valor da variavel:"+c.getVariableLabel()+"  foi:" + c.getTruth());
                                }
                            }
                            else
                            {
                                PerguntaMessaging pergunta = new PerguntaMessaging();
                                Mensagem mensagem = new Mensagem();
                                mensagem.setTipo("RESPOSTA");
                                mensagem.setObjeto("" + rule.getTruth());
                                mensagem.setTipoAvisoDepuracao("REGRA");
                                mensagem.setPosicao("" + rule.getLabel());
                                pergunta.executeAskNotification(mensagem);
                                System.out.println("valor da variavel:"+c.getVariableLabel()+"  foi:" + c.getTruth());
                            }

                        }

                    }
                }
                catch (NumberFormatException e1)
                {
                    e1.printStackTrace();
                }
                catch (InvalidOperatorException e1)
                {
                    e1.printStackTrace();
                }

                rule.update();

                if (rule.getTruth() != null)
                {
                    PerguntaMessaging pergunta = new PerguntaMessaging();
                    Mensagem mensagem = new Mensagem();
                    mensagem.setTipo("RESPOSTA");
                    mensagem.setObjeto("" + rule.getTruth());
                    mensagem.setTipoAvisoDepuracao("REGRA");
                    mensagem.setPosicao(rule.getLabel());
                    pergunta.executeAskNotification(mensagem);
                    System.out.println("valor da regra:"+rule.getLabel()+"  foi:" + rule.getTruth());

                    pergunta = new PerguntaMessaging();
                    mensagem = new Mensagem();
                    mensagem.setTipo("RESPOSTA");
                    mensagem.setObjeto("" + rule.getTruth());
                    mensagem.setTipoAvisoDepuracao("ITEM");
                    mensagem.setPosicao("" + rule.getConsequent().getIditemregra());
                    pergunta.executeAskNotification(mensagem);
                    System.out.println("valor da variavel:"+rule.getConsequent().getVariableLabel()+"  foi:" + rule.getConsequent().getTruth());
                    //se a regra e verdadeira, dispara o consequente
                    if (rule.getTruth())
                    {
                        rule.fire(wm);
                    }
                   
                }
                goalStack.pop();
            }
        }
        RuleVariable objetivoFinal = wm.getVariable(varivelObjetivo);
        if (objetivoFinal != null)
        {
            PerguntaMessaging pergunta = new PerguntaMessaging();
            Mensagem mensagem = new Mensagem();
            mensagem.setTipo("RESULTADO");
            Collection<RuleVariable> allVariables = wm.getAllVariables();
            List<String> resultado = new ArrayList<String>();

            for (RuleVariable variable2 : allVariables)
            {
                resultado.add(variable2.getLabel() + " = " + variable2.getValue());
            }
            mensagem.setResultado(resultado);
            mensagem.setResultadoFinal(objetivoFinal.getLabel() + " = " + objetivoFinal.getValue());
            pergunta.executeAskNotification(mensagem);
        }

    }

    private void enviaNotificacaoDepuracao(String tipoObj, Object objeto, String posicao)
    {
        PerguntaMessaging pergunta = new PerguntaMessaging();
        Mensagem mensagem = new Mensagem();
        mensagem.setTipo("DEPURACAO");
        mensagem.setTipoAvisoDepuracao(tipoObj);
        mensagem.setObjeto(objeto);
        mensagem.setPosicao(posicao);
        pergunta.executeAskNotification(mensagem);

    }

    private void fazPergutaParaUsuario(Clause c)
    {
        PerguntaMessaging pergunta = new PerguntaMessaging();

        String sql = "select v from Variavel v "
                + " WHERE v.variavel = :v_variavel";
        Query query = em.createQuery(sql);
        query.setParameter("v_variavel", c.getVariableLabel());
        Variavel v = (Variavel) query.getSingleResult();

        Mensagem mensagem = new Mensagem();
        mensagem.setTipo("PERGUNTA");
        mensagem.setVariavel(v);
        sql = "select i from ItemRegra i"
                + " where i.iditemregra = :v_iditemregra";
        query = em.createQuery(sql);
        query.setParameter("v_iditemregra", ((BooleanClause) c).getIditemregra());
        ItemRegra itemRegra = (ItemRegra) query.getSingleResult();
        mensagem.setItemRegra(itemRegra);

        pergunta.executeAskNotification(mensagem);

    }

    @Override
    public void respondeu(Resposta resposta)
    {
        BooleanClause c = (BooleanClause) clausulaAtual;
        wm.addVariable(new RuleVariable(c.getVariableLabel(), resposta.getValor().getValor()));

        execute();
    }
}
