/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.*;
import br.com.expert.model.Base;
import br.com.expert.remote.ItemRegraEAORemote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author jean.siqueira
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ItemRegraEAO implements ItemRegraEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public ItemRegraEAO()
    {
    }

    @Override
    public ItemRegra adicionarItemRegra(ItemRegra regraRemota) throws ServiceException
    {

        try
        {
            System.out.println("regra:" + regraRemota.getIdregra());
            em.merge(regraRemota);
            em.flush();

            return regraRemota;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarItemRegra(ItemRegra regra) throws ServiceException
    {
        try
        {
            em.merge(regra);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirItemRegra(ItemRegra regra) throws ServiceException
    {
        try
        {
            ItemRegra entityToBeRemoved = em.merge(regra);
            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<ItemRegra> consultarItensRegraPorRegra(Regra regra) throws ServiceException
    {
        try
        {
            String sql = "select r from ItemRegra r "
                    + " WHERE r.idregra = :idregra";
            Query query = em.createQuery(sql);
            query.setParameter("idregra", regra);

            List<ItemRegra> itens = (List<ItemRegra>) query.getResultList();
            Map<String, String> estrutura = new HashMap<String, String>();
            Map<String, String> estrutura2 = new HashMap<String, String>();
            String variavelValor;
            boolean inconsistente = false;
            
            int pos = 0;
            for (ItemRegra item : itens)
            {
                if(pos==0)
                    item.setIdconectivo(null);
                pos++;
                variavelValor = item.getIdvariavel().getIdvariavel() + "-" + item.getIdvalor().getIdvalor();
                if (estrutura.containsKey(variavelValor))
                {
                    if (item.getConclusao())
                    {
                        item.setErro("Esta variável já foi utilizada na cláusula.\n"
                                + "Não é permitido concluir regras, com variáveis utilizadas na estrutura!");
                        item.setInconsistente(true);
                        inconsistente = true;
                    }
                    else
                    {
                        item.setErro("Clásula esta repetida!");
                        item.setInconsistente(true);
                        inconsistente = true;
                    }
                }

                if (estrutura2.containsKey("" + item.getIdvariavel().getIdvariavel()))
                {
                    boolean conectivo = false;
                    if (item.getIdconectivo() != null)
                    {
                        if (item.getIdconectivo().getConectivo().equals("OU"))
                        {
                            conectivo = false;
                        }
                        else
                        {
                            conectivo = true;
                        }
                    }
                    else
                    {
                        conectivo = true;
                    }
                    if (!estrutura2.get("" + item.getIdvariavel().getIdvariavel()).equals("" + item.getIdvalor().getIdvalor())
                            && conectivo)
                    {
                        item.setErro("Atenção: Esta variável já foi utilizada na regra, mas com outro valor!");
                        item.setInconsistente(true);
                        inconsistente = true;

                    }

                }

                estrutura2.put("" + item.getIdvariavel().getIdvariavel(), "" + item.getIdvalor().getIdvalor());

                estrutura.put(variavelValor, null);

                if (!inconsistente)
                {

                    item.setErro(null);
                    item.setInconsistente(false);

                }

                em.merge(item);
                
            }
            em.flush();

            return itens;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
