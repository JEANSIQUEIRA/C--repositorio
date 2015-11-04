/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Pessoa;
import br.com.expert.remote.PessoaEAORemote;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author cicero.bispo
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class PessoaEAO implements PessoaEAORemote {

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public PessoaEAO() {
    }

    @Override
    public Pessoa adicionarPessoa(Pessoa ps) throws ServiceException {
        try {
            em.persist(ps);
            em.flush();
            return ps;
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarPessoa(Pessoa ps) throws ServiceException {
        try {
            em.merge(ps);
            em.flush();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirPessoa(Pessoa ps) throws ServiceException {
       try
        {
            Pessoa entityToBeRemoved = em.merge(ps);

            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Pessoa> consultarPessoas() throws ServiceException {
          try
        {
            String sql = "select p from Pessoa p ";
            Query query = em.createQuery(sql);
            em.flush();
            return query.getResultList();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Pessoa> consultarPessoaPorNome(String nome) throws ServiceException {
         try
        {
            String sql = "select p from Pessoa p "
                    + " where p.nome like :nome";
            Query query = em.createQuery(sql);
            query.setParameter("nome", "%" + nome + "%");
            em.flush();
            return query.getResultList();

        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
