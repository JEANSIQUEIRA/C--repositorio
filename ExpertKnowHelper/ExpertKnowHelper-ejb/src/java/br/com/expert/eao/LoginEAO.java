/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Pessoa;
import br.com.expert.model.Usuario;
import br.com.expert.model.UsuarioGrupo;
import br.com.expert.remote.LoginEAORemote;
import br.com.expert.remote.UsuarioEAORemote;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author cicero.bispo
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class LoginEAO implements LoginEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    @Override
    public Usuario logar(Usuario usr) throws ServiceException
    {
        try
        {
            String sql = "select u from Usuario u "
                    + " where u.login = :login "
                    + " and u.senha = :senha";
            Query query = em.createQuery(sql);
            query.setParameter("login", usr.getLogin());
            query.setParameter("senha", usr.getSenha());
            //em.flush();
            try
            {
                return (Usuario) query.getSingleResult();
            }
            catch (NoResultException nre)
            {
                //Ignore this because as per your logic this is ok!
                return null;
            }
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<UsuarioGrupo> obterGruposDoUsuario(Usuario usuario) throws ServiceException
    {
        try
        {
            String sql = "select g from UsuarioGrupo g "
                    + " where g.idpessoa = :idpesoa";
            Query query = em.createQuery(sql);
            query.setParameter("idpesoa", usuario.getIdpessoa());
            em.flush();
            return query.getResultList();

        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
