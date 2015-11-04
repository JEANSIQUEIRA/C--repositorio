/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Grupousuarios;
import br.com.expert.remote.GrupousuariosEAORemote;
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
public class GrupousuariosEAO implements GrupousuariosEAORemote {

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public GrupousuariosEAO() {
    }

    @Override
    public Grupousuarios adicionarGrupo(Grupousuarios grupo) throws ServiceException {
        try {
            em.persist(grupo);
            em.flush();
            return grupo;
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarGrupo(Grupousuarios grupo) throws ServiceException {
        try {
            em.merge(grupo);
            em.flush();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirGrupo(Grupousuarios grupo) throws ServiceException {
        try {
            Grupousuarios entityToBeRemoved = em.merge(grupo);

            em.remove(entityToBeRemoved);
            em.flush();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public List<Grupousuarios> consultarGrupos() throws ServiceException {
        try {
            String sql = "select gru from Grupousuarios gru ";
            Query query = em.createQuery(sql);
            em.flush();
            return query.getResultList();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }

    }

    @Override
    public List<Grupousuarios> consultarGruposPorNome(String nome) throws ServiceException {
        try {
            String sql = "select gru from Grupousuarios gru "
                    + " where gru.nomegrupo like :nome";
            Query query = em.createQuery(sql);
            query.setParameter("nome", "%" + nome + "%");
            em.flush();
            return query.getResultList();

        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }
}
