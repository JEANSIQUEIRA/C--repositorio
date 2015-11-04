/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.expert.eao;

import br.com.expert.model.Base;
import br.com.expert.model.Conectivo;
import br.com.expert.model.Operador;
import br.com.expert.remote.ConectivoEAORemote;
import br.com.expert.remote.OperadorEAORemote;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author jean.siqueira
 */
@Stateless
//@TransactionAttribute(TransactionAttributeType.REQUIRED)
//@TransactionManagement(TransactionManagementType.CONTAINER)
public class ConectivoEAO implements ConectivoEAORemote {

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public ConectivoEAO() {
    }

    @Override
    public List<Conectivo> consultarConectivos() {
        String sql = "select c from Conectivo c ";
        Query query = em.createQuery(sql);

        return query.getResultList();
    }
}
