/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.*;
import br.com.expert.model.Base;
import br.com.expert.remote.VariavelEAORemote;
import java.util.ArrayList;
import java.util.List;
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
public class VariavelEAO implements VariavelEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public VariavelEAO()
    {
    }

    @Override
    public Variavel adicionarVariavel(Variavel variavelRemota) throws ServiceException
    {
        try
        {
            List<Valor> valores = new ArrayList<Valor>();
            for (Valor valor : variavelRemota.getValoresList())
            {
                Valor v = new Valor();
                v.setIdvariavel(variavelRemota);
                v.setValor(valor.getValor());
                valores.add(v);
            }
            variavelRemota.setValoresList(valores);
            em.persist(variavelRemota);
            em.flush();

            return variavelRemota;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarVariavel(Variavel variavel) throws ServiceException
    {
        try
        {
            em.merge(variavel);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirVariavel(Variavel variavel) throws ServiceException
    {
        try
        {
            Variavel entityToBeRemoved = em.merge(variavel);
            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Variavel> consultarVariaveisPorBase(Base base) throws ServiceException
    {
        try
        {
            String sql = "select v from Variavel v "
                    + " WHERE v.idbase = :idbase";
            Query query = em.createQuery(sql);
            query.setParameter("idbase", base);
            em.flush();
            return query.getResultList();
        }
        catch (Exception pex) 
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Instancia> obterInstancias() throws ServiceException
    {
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Instancia.class));
            em.flush();
            return em.createQuery(cq).getResultList();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
