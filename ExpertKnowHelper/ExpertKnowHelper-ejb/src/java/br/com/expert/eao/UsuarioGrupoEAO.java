package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Base;
import br.com.expert.model.Grupousuarios;
import br.com.expert.model.ItemRegra;
import br.com.expert.model.Regra;
import br.com.expert.model.UsuarioGrupo;
import br.com.expert.model.Valor;
import br.com.expert.model.VariaveisNaoUtilizadas;
import br.com.expert.model.Variavel;
import br.com.expert.remote.BaseEAORemote;
import br.com.expert.remote.UsuarioGrupoEAORemote;
import eureka.base.Clause;
import eureka.base.Rule;
import eureka.base.RuleParser;
import eureka.dao.ClauseDAO;
import eureka.dao.FacadeDAO;
import eureka.dao.RuleDAO;
import eureka.util.HibernateUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 *
 * @author jean.siqueira
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class UsuarioGrupoEAO implements UsuarioGrupoEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public UsuarioGrupoEAO()
    {
    }

    @Override
    public UsuarioGrupo adicionarUsuarioGrupo(UsuarioGrupo ug) throws ServiceException
    {
        em.persist(ug);
        em.flush();
        return ug;
    }

    @Override
    public void atualizarUsuarioGrupo(UsuarioGrupo ug) throws ServiceException
    {
        try
        {
            em.merge(ug);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirGrupo(br.com.expert.model.UsuarioGrupo usuarioGrupo) throws ServiceException
    {
        try
        {
            br.com.expert.model.UsuarioGrupo entityToBeRemoved = em.merge(usuarioGrupo);

            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<UsuarioGrupo> consultarUsuarioGrupoPorGrupo(Grupousuarios grupo) throws ServiceException
    {
        try
        {
            String sql = "select g from UsuarioGrupo g "
                    + " where g.idgrusuario = :idgrusuario";
            Query query = em.createQuery(sql);
            query.setParameter("idgrusuario", grupo);
            em.flush();
            return query.getResultList();

        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }
}
