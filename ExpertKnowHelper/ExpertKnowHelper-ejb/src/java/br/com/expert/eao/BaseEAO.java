package br.com.expert.eao;

import br.com.expert.exception.ServiceException;
import br.com.expert.model.Base;
import br.com.expert.model.ItemRegra;
import br.com.expert.model.Regra;
import br.com.expert.model.Valor;
import br.com.expert.model.VariaveisNaoUtilizadas;
import br.com.expert.model.Variavel;
import br.com.expert.remote.BaseEAORemote;
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
public class BaseEAO implements BaseEAORemote
{

    @PersistenceContext(unitName = "ExpertKnowHelper-ejbPU")
    private EntityManager em;

    public BaseEAO()
    {
    }

    @Override
    public Base adicionarBase(Base baseRemota) throws ServiceException
    {
        try
        {
            baseRemota.setDtcriacao(new Date());
            em.persist(baseRemota);
            em.flush();
            return baseRemota;
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void atualizarBase(Base base) throws ServiceException
    {
        try {
            em.merge(base);
            em.flush();
        } catch (Exception pex) {
            throw new ServiceException(pex);
        }
    }

    @Override
    public void excluirBase(Base base) throws ServiceException
    {
        try
        {
            Base entityToBeRemoved = em.merge(base);

            em.remove(entityToBeRemoved);
            em.flush();
        }
        catch (Exception pex)
        {
            throw new ServiceException(pex);
        }

    }

    @Override
    public List<Base> consultarBasePorNome(String nome) throws ServiceException
    {
        try
        {
            String sql = "select b from Base b "
                    + " where b.nome like :nome";
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
    public List<Base> consultarBases() throws ServiceException
    {
        try
        {
            String sql = "select b from Base b ";
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
    public String gerarSistemaEspecialista(Base base) throws ServiceException
    {
        try
        {
            String validar = validarSistemaEspecialista(base);
            if (validar.isEmpty())
            {
                HibernateUtil.beginTransaction();

                RuleDAO r = FacadeDAO.getFacadeDAO().getRuleDAO();
                r.removeAll(r.findAll());

                ClauseDAO c = FacadeDAO.getFacadeDAO().getClauseDAO();
                c.removeAll(c.findAll());

                HibernateUtil.commit();

                String sql = "select r from Regra r "
                        + " WHERE r.idbase = :idbase";
                Query query = em.createQuery(sql);
                query.setParameter("idbase", base);
                List<Regra> regras = query.getResultList();

                StringBuilder strRegra = new StringBuilder();
                for (Regra regra : regras)
                {
                    strRegra.append(regra.getRegra()).append(" : ");
                    List<ItemRegra> itens = regra.getItensRegraList();

                    for (ItemRegra item : itens)
                    {
                        if (item.getIdconectivo() == null && !item.getConclusao())
                        {
                            strRegra.append(" SE ");
                        }
                        else if (item.getIdconectivo() == null && item.getConclusao())
                        {
                            strRegra.append(" ENTAO ");
                        }
                        else
                        {
                            strRegra.append(item.getIdconectivo().getConectivo()).append(" ");
                        }
                        if (!item.getConclusao())
                        {
                            strRegra.append(item.getIdvariavel().getVariavel()).append(" ").append(item.getIdoperador().getOperador()).append(" ");
                        }
                        else
                        {
                            strRegra.append(item.getIdvariavel().getVariavel()).append(" <- ");
                        }
                        strRegra.append(item.getIdvalor().getValor());
                        strRegra.append("|" + item.getIdItemRegra());
                        strRegra.append("\n");
                    }
                    strRegra.append(";");
                }
                RuleParser.loadRulesOnDatabase(strRegra.toString(), base.getIdbase());

                base.setCriado(true);
                em.merge(base);

                em.flush();
                return "";
            }
            else
            {
                return validar;
            }

        }
        catch (Exception pex)
        {
            HibernateUtil.rollback();
            pex.printStackTrace();
            throw new ServiceException(pex);
        }

    }

    private String validarSistemaEspecialista(Base base)
    {
        em.clear();

        String sql = "select itensregra.iditemregra"
                + " from itensregra"
                + " inner join regras on (regras.idregra=itensregra.idregra)"
                + " inner join bases on (bases.idbase=regras.idbase)"
                + " where bases.idbase=" + base.getIdbase()
                + " and itensregra.inconsistente=1";

        Query query = em.createNativeQuery(sql);

        if (query.getResultList().size() > 0)
        {
            return "Existem incosnsitencias nas regras. Por favor, revise as regras observando o status de cada uma.";
        }
        else
        {

            sql = "select r from Regra r "
                    + " WHERE r.idbase = :idbase";
            query = em.createQuery(sql);
            query.setParameter("idbase", base);
            List<Regra> regras = query.getResultList();
            Map<Integer, Valor> variaveisUtilizadas = new HashMap<Integer, Valor>();
            Map<Integer, Integer> vars = new HashMap<Integer, Integer>();
            Map<Integer, Valor> valoresDisponiveis = new HashMap<Integer, Valor>();
            StringBuilder avisos = new StringBuilder("Inconsistências nas regras:\n");
            boolean possuiSaida = false;
            boolean possuiCondicao = false;
            boolean inconsistencia = false;
            boolean objetivoEhSaida = false;
            for (Regra regra : regras)
            {
                em.refresh(regra);
                possuiSaida = false;
                possuiCondicao = false;
                inconsistencia = false;

                for (ItemRegra itemRegra : regra.getItensRegraList())
                {
                    em.refresh(itemRegra);
                    vars.put(itemRegra.getIdvariavel().getIdvariavel(), null);
                    variaveisUtilizadas.put(itemRegra.getIdvalor().getIdvalor(), itemRegra.getIdvalor());
                    if (itemRegra.getConclusao())
                    {
                        possuiSaida = true;
                        if (itemRegra.getIdvariavel().getObjetivo())
                        {
                            objetivoEhSaida = true;
                        }
                    }
                    else
                    {
                        possuiCondicao = true;
                    }
                }
                if (!possuiSaida)
                {
                    avisos.append("'" + regra.getRegra() + "' não possui variável de conclusão!\n");
                    inconsistencia = true;
                }

                if (!possuiCondicao)
                {
                    avisos.append("'" + regra.getRegra() + "' não possui variável de condição!\n");
                    inconsistencia = true;
                }

            }
            if (!objetivoEhSaida)
            {
                avisos.append("A variável objetivo não está mapeada em nenhuma REGRA!\n");
                inconsistencia = true;
            }

            if (!inconsistencia)
            {
                avisos.setLength(0);
            }

            sql = "select v from Variavel v "
                    + " WHERE v.idbase = :idbase";
            query = em.createQuery(sql);
            query.setParameter("idbase", base);
            List<Variavel> variaveis = query.getResultList();

            for (Variavel variavel : variaveis)
            {
                for (Valor valor : variavel.getValoresList())
                {
                    if (vars.containsKey(valor.getIdvariavel().getIdvariavel()))
                    {
                        valoresDisponiveis.put(valor.getIdvalor(), valor);
                    }
                }
            }

            Map<Integer, List<Valor>> naoMapeadas = new HashMap<Integer, List<Valor>>();
            for (Map.Entry<Integer, Valor> entryValores : valoresDisponiveis.entrySet())
            {
                if (!variaveisUtilizadas.containsKey(entryValores.getKey()))
                {
                    List<Valor> valores = null;
                    if (naoMapeadas.containsKey(entryValores.getValue().getIdvariavel().getIdvariavel()))
                    {
                        valores = naoMapeadas.get(entryValores.getValue().getIdvariavel().getIdvariavel());
                    }
                    else
                    {
                        valores = new ArrayList<Valor>();
                        naoMapeadas.put(entryValores.getValue().getIdvariavel().getIdvariavel(), valores);
                    }
                    valores.add(entryValores.getValue());
                }
            }

            if (!naoMapeadas.isEmpty())
            {
                avisos.append("Variáveis/Valores não mapeados!\n");
            }

            for (Map.Entry<Integer, List<Valor>> entryValores : naoMapeadas.entrySet())
            {
                avisos.append("Variável:" + entryValores.getValue().get(0).getIdvariavel().getVariavel()).append(". Valores:");
                int cont = 0;
                for (Valor valor : entryValores.getValue())
                {
                    avisos.append(valor.getValor());
                    if (cont < entryValores.getValue().size() - 1)
                    {
                        avisos.append(",");
                    }
                    cont++;
                }
                avisos.append("\n");
            }
            em.flush();
            return avisos.toString();
        }

    }
}
