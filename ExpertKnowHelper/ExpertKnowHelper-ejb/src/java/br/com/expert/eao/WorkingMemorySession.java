/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.facade.WorkingMemoryRemote;
import br.com.expert.model.Base;
import br.com.expert.model.ItemRegra;
import br.com.expert.model.Regra;
import br.com.expert.model.Resposta;
import br.com.expert.model.Variavel;
import br.com.expert.remote.BaseEAORemote;
import eureka.base.WorkingMemory;
import eureka.engine.BackwardChaining;
import eureka.engine.Engine;
import eureka.util.HibernateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author jean.siqueira
 */
@Stateful
public class WorkingMemorySession implements WorkingMemoryRemote
{

    WorkingMemory wm;
    Engine e;
    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;
    @EJB
    private BaseEAORemote baseEao;

    @Override
    public List<ItemRegra> iniciarMaquinaInferencia(Base base) throws ServiceException
    {
        try
        {
            String validacao = baseEao.gerarSistemaEspecialista(base);
            if (validacao.equals(""))
            {

                wm = new WorkingMemory();

                String sql = "select v from Variavel v "
                        + " WHERE v.idbase = :idbase"
                        + " AND v.objetivo = :objetivo";
                Query query = em.createQuery(sql);
                query.setParameter("idbase", base);
                query.setParameter("objetivo", true);
                Variavel variavel = (Variavel) query.getSingleResult();

                e = backwardTest(wm, variavel.getVariavel(), null);
                e.execute();
                return consultarItensRegraPorRegra(base);
            }
            else
                throw new ServiceException("Base de conhecimento está inválida\n. Detalhes:"+validacao);
        }
        catch (ServiceException ex)
        {
            Logger.getLogger(WorkingMemorySession.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public List<ItemRegra> consultarItensRegraPorRegra(Base base)
    {

        /*String sql = "select r from ItemRegra r "
         + " left join r.idregra p"
         //+ " left join p.idbase b"
         + " WHERE p.idbase = :idbase";
         Query query = em.createQuery(sql);
         query.setParameter("idbase", base);

         List<ItemRegra> itens = (List<ItemRegra>) query.getResultList();

         List<ItemRegra> itens2 = new ArrayList<ItemRegra>();

         String regra = itens.get(0).getIdregra().getRegra();
         ItemRegra itemRegra = new ItemRegra();
         Variavel v = new Variavel();
         v.setVariavel(regra);
         itemRegra.setIdvariavel(v);
         itens2.add(itemRegra);
         for (ItemRegra item : itens)
         {
         if(!item.getIdregra().getRegra().equals(regra))
         {
         ItemRegra itemRegra2 = new ItemRegra();
         Variavel v2 = new Variavel();
         v.setVariavel(item.getIdregra().getRegra());
         itemRegra.setIdvariavel(v2);
         itens2.add(itemRegra2);
         }
         itens2.add(item);
         }*/


        String sql = "select r from Regra r "
                + " WHERE r.idbase = :idbase";
        Query query = em.createQuery(sql);
        query.setParameter("idbase", base);
        List<Regra> regras = query.getResultList();

        StringBuilder strRegra = new StringBuilder();
        List<ItemRegra> itens2 = new ArrayList<ItemRegra>();
        for (Regra regra : regras)
        {
            em.refresh(regra);
        
            ItemRegra itemRegra = new ItemRegra();
            Variavel v = new Variavel();
            v.setVariavel(regra.getRegra());
            itemRegra.setIdvariavel(v);
            itens2.add(itemRegra);
            for (ItemRegra itemRegra2 : regra.getItensRegraList())
            {
                em.refresh(itemRegra2);
                itens2.add(itemRegra2);
            }
        }
        return itens2;
    }

    private Engine backwardTest(WorkingMemory wm, String variable, String value)
    {
        BackwardChaining b = new BackwardChaining();
        b.em = this.em;

        Hashtable<Object, Object> params = new Hashtable<Object, Object>();
        if (variable != null)
        {
            params.put(BackwardChaining.GOAL_VARIABLE, variable);
        }
        if (value != null)
        {
            params.put(BackwardChaining.GOAL_VALUE, value);
        }

        b.init(wm, params, variable);

        return b;
    }

    @Override
    public void inicializarSE()
    {

        wm = new WorkingMemory();
        e = backwardTest(wm, "devo_ir_à_praia", null);

        e.execute();
    }

    @Override
    public void responder(Resposta resposta)
    {
        e.respondeu(resposta);
    }

    @Override
    public String mostrarResultado()
    {
        wm.printState();
        return "";
    }
}
