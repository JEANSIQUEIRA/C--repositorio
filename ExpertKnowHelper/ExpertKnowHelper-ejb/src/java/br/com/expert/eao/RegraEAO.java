/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.*;
import br.com.expert.model.Base;
import br.com.expert.remote.RegraEAORemote;
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
public class RegraEAO implements RegraEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public RegraEAO()
    {
    }

    @Override
    public Regra adicionarRegra(Regra regraRemota) throws ServiceException
    {

        try
        {
            em.persist(regraRemota);
            em.flush();
            return regraRemota;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarRegra(Regra regra) throws ServiceException
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
    public void excluirRegra(Regra regra) throws ServiceException
    {
        try
        {
            Regra entityToBeRemoved = em.merge(regra);
            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Regra> consultarRegrasPorBase(Base base) throws ServiceException
    {
        try
        {
            String sql = "select r from Regra r "
                    + " WHERE r.idbase = :idbase";
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
    public List<Regra> consultarRegras() throws ServiceException
    {
        try
        {
            String sql = "select r from Regra r ";
            Query query = em.createQuery(sql);
            em.flush();
            return query.getResultList();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
