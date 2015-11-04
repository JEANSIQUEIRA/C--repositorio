/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Pessoa;
import br.com.expert.model.Usuario;
import br.com.expert.remote.UsuarioEAORemote;
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
public class UsuarioEAO implements UsuarioEAORemote {

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    @Override
    public Usuario adicionarUsuario(Usuario usrs) throws ServiceException {
        try
        {
            em.persist(usrs);
            em.flush();
            return usrs;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarUsuario(Usuario usrs) throws ServiceException {
        try {
            em.merge(usrs);
            em.flush();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirUsuario(Usuario usrs) throws ServiceException {
          try
        {
            Usuario entityToBeRemoved = em.merge(usrs);

            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Usuario> consultarUsuarios() throws ServiceException {
         try
        {
            String sql = "select use from Usuario use ";
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
    public List<Usuario> consultarUsuarioPorNome(String nome) throws ServiceException {
       try
        {
            String sql = "select user from Usuario user "
                    + " where user.pessoa.nome like :nome";
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

    @Override
    public Usuario consultarUsuarioPorPessoa(Pessoa pessoa) throws ServiceException
    {
        try
        {
            String sql = "select u from Usuario u "
                    + " where u.idpessoa=:idpessoa";
            Query query = em.createQuery(sql);
            query.setParameter("idpessoa", pessoa);
            em.flush();
            List<Usuario> usuarios = query.getResultList();
            if(usuarios.size() > 0)
                return usuarios.get(0);
            else
                return null;
            
        }
        catch (Exception pex)
        {
            pex.printStackTrace();
            throw new ServiceException(pex);
        }
    }
}
