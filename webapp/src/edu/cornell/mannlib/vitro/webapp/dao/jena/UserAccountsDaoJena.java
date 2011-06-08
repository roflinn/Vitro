/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;

/**
 * Implement UserAccountsDao for Jena models.
 */
public class UserAccountsDaoJena extends JenaBaseDao implements UserAccountsDao {
	public UserAccountsDaoJena(WebappDaoFactoryJena wadf) {
		super(wadf);
	}

	@Override
	protected OntModel getOntModel() {
		return getOntModelSelector().getUserAccountsModel();
	}

	@Override
	public Collection<UserAccount> getAllUserAccounts() {
		List<String> userUris = new ArrayList<String>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmts = getOntModel().listStatements((Resource) null,
					RDF.type, USERACCOUNT);
			while (stmts.hasNext()) {
				Resource subject = stmts.next().getSubject();
				if (subject != null) {
					userUris.add(subject.getURI());
				}
			}
			stmts.close();
		} finally {
			getOntModel().leaveCriticalSection();
		}

		List<UserAccount> userAccounts = new ArrayList<UserAccount>();
		for (String userUri : userUris) {
			UserAccount ua = getUserAccountByUri(userUri);
			if (ua != null) {
				userAccounts.add(ua);
			}
		}

		return userAccounts;
	}

	@Override
	public UserAccount getUserAccountByUri(String uri) {
		if (uri == null) {
			return null;
		}

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			OntResource r = getOntModel().getOntResource(uri);
			if (r == null) {
				return null;
			}

			if (!isResourceOfType(r, USERACCOUNT)) {
				return null;
			}

			UserAccount u = new UserAccount();
			u.setUri(r.getURI());
			u.setEmailAddress(getPropertyStringValue(r,
					USERACCOUNT_EMAIL_ADDRESS));
			u.setFirstName(getPropertyStringValue(r, USERACCOUNT_FIRST_NAME));
			u.setLastName(getPropertyStringValue(r, USERACCOUNT_LAST_NAME));
			u.setMd5Password(getPropertyStringValue(r, USERACCOUNT_MD5_PASSWORD));
			u.setOldPassword(getPropertyStringValue(r, USERACCOUNT_OLD_PASSWORD));
			u.setPasswordLinkExpires(getPropertyLongValue(r,
					USERACCOUNT_PASSWORD_LINK_EXPIRES));
			u.setPasswordChangeRequired(getPropertyBooleanValue(r,
					USERACCOUNT_PASSWORD_CHANGE_REQUIRED));
			u.setLoginCount(getPropertyIntValue(r, USERACCOUNT_LOGIN_COUNT));
			u.setStatusFromString(getPropertyStringValue(r, USERACCOUNT_STATUS));
			u.setExternalAuthId(getPropertyStringValue(r,
					USERACCOUNT_EXTERNAL_AUTH_ID));
			u.setPermissionSetUris(getPropertyResourceURIValues(r,
					USERACCOUNT_HAS_PERMISSION_SET));
			return u;
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public UserAccount getUserAccountByEmail(String emailAddress) {
		if (emailAddress == null) {
			return null;
		}

		String userUri = null;

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmts = getOntModel().listStatements(null,
					USERACCOUNT_EMAIL_ADDRESS,
					getOntModel().createLiteral(emailAddress));
			if (stmts.hasNext()) {
				userUri = stmts.next().getSubject().getURI();
			}
			stmts.close();
		} finally {
			getOntModel().leaveCriticalSection();
		}

		return getUserAccountByUri(userUri);
	}

	@Override
	public UserAccount getUserAccountByExternalAuthId(String externalAuthId) {
		if (externalAuthId == null) {
			return null;
		}

		String userUri = null;

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmts = getOntModel().listStatements(null,
					USERACCOUNT_EXTERNAL_AUTH_ID,
					getOntModel().createLiteral(externalAuthId));
			if (stmts.hasNext()) {
				userUri = stmts.next().getSubject().getURI();
			}
			stmts.close();
		} finally {
			getOntModel().leaveCriticalSection();
		}

		return getUserAccountByUri(userUri);
	}

	@Override
	public boolean isRootUser(UserAccount userAccount) {
		if (userAccount == null) {
			return false;
		}

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			OntResource r = getOntModel().getOntResource(userAccount.getUri());
			return isResourceOfType(r, USERACCOUNT_ROOT_USER);
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public String insertUserAccount(UserAccount userAccount) {
		if (userAccount == null) {
			throw new NullPointerException("userAccount may not be null.");
		}
		if (!userAccount.getUri().isEmpty()) {
			throw new IllegalArgumentException(
					"URI of new userAccount must be empty.");
		}

		OntModel model = getOntModel();

		model.enterCriticalSection(Lock.WRITE);
		try {
			String userUri = getUnusedURI();
			Resource res = model.createIndividual(userUri, USERACCOUNT);
			addPropertyStringValue(res, USERACCOUNT_EMAIL_ADDRESS,
					userAccount.getEmailAddress(), model);
			addPropertyStringValue(res, USERACCOUNT_FIRST_NAME,
					userAccount.getFirstName(), model);
			addPropertyStringValue(res, USERACCOUNT_LAST_NAME,
					userAccount.getLastName(), model);
			addPropertyStringValue(res, USERACCOUNT_MD5_PASSWORD,
					userAccount.getMd5Password(), model);
			addPropertyStringValue(res, USERACCOUNT_OLD_PASSWORD,
					userAccount.getOldPassword(), model);
			addPropertyLongValue(res, USERACCOUNT_PASSWORD_LINK_EXPIRES,
					userAccount.getPasswordLinkExpires(), model);
			addPropertyBooleanValue(res, USERACCOUNT_PASSWORD_CHANGE_REQUIRED,
					userAccount.isPasswordChangeRequired(), model);
			addPropertyIntValue(res, USERACCOUNT_LOGIN_COUNT,
					userAccount.getLoginCount(), model);
			if (userAccount.getStatus() != null) {
				addPropertyStringValue(res, USERACCOUNT_STATUS, userAccount
						.getStatus().toString(), model);
			}
			addPropertyStringValue(res, USERACCOUNT_EXTERNAL_AUTH_ID,
					userAccount.getExternalAuthId(), model);
			updatePropertyResourceURIValues(res,
					USERACCOUNT_HAS_PERMISSION_SET,
					userAccount.getPermissionSetUris(), model);

			userAccount.setUri(userUri);
			return userUri;
		} catch (InsertException e) {
			log.error(e, e);
			return null;
		} finally {
			model.leaveCriticalSection();
		}
	}

	@Override
	public void updateUserAccount(UserAccount userAccount) {
		if (userAccount == null) {
			throw new NullPointerException("userAccount may not be null.");
		}

		OntModel model = getOntModel();

		model.enterCriticalSection(Lock.WRITE);
		try {
			OntResource res = model.getOntResource(userAccount.getUri());
			if (res == null) {
				throw new IllegalArgumentException("userAccount '"
						+ userAccount.getUri() + "' does not exist.");
			}

			updatePropertyStringValue(res, USERACCOUNT_EMAIL_ADDRESS,
					userAccount.getEmailAddress(), model);
			updatePropertyStringValue(res, USERACCOUNT_FIRST_NAME,
					userAccount.getFirstName(), model);
			updatePropertyStringValue(res, USERACCOUNT_LAST_NAME,
					userAccount.getLastName(), model);
			updatePropertyStringValue(res, USERACCOUNT_MD5_PASSWORD,
					userAccount.getMd5Password(), model);
			updatePropertyStringValue(res, USERACCOUNT_OLD_PASSWORD,
					userAccount.getOldPassword(), model);
			updatePropertyLongValue(res, USERACCOUNT_PASSWORD_LINK_EXPIRES,
					userAccount.getPasswordLinkExpires(), model);
			updatePropertyBooleanValue(res,
					USERACCOUNT_PASSWORD_CHANGE_REQUIRED,
					userAccount.isPasswordChangeRequired(), model, true);
			updatePropertyIntValue(res, USERACCOUNT_LOGIN_COUNT,
					userAccount.getLoginCount(), model);
			if (userAccount.getStatus() == null) {
				updatePropertyStringValue(res, USERACCOUNT_STATUS, null, model);
			} else {
				updatePropertyStringValue(res, USERACCOUNT_STATUS, userAccount
						.getStatus().toString(), model);
			}
			updatePropertyStringValue(res, USERACCOUNT_EXTERNAL_AUTH_ID,
					userAccount.getExternalAuthId(), model);
			updatePropertyResourceURIValues(res,
					USERACCOUNT_HAS_PERMISSION_SET,
					userAccount.getPermissionSetUris(), model);
		} finally {
			model.leaveCriticalSection();
		}
	}

	@Override
	public void deleteUserAccount(String userAccountUri) {
		if (userAccountUri == null) {
			return;
		}

		OntModel model = getOntModel();

		model.enterCriticalSection(Lock.WRITE);
		try {
			Resource res = model.createResource(userAccountUri);
			model.removeAll(res, null, null);
		} finally {
			model.leaveCriticalSection();
		}
	}

	@Override
	public PermissionSet getPermissionSetByUri(String uri) {
		if (uri == null) {
			return null;
		}

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			OntResource r = getOntModel().getOntResource(uri);
			if (r == null) {
				return null;
			}
			if (!isResourceOfType(r, PERMISSIONSET)) {
				return null;
			}

			PermissionSet ps = new PermissionSet();
			ps.setUri(uri);
			ps.setLabel(getPropertyStringValue(r, RDFS.label));
			ps.setPermissionUris(getPropertyResourceURIValues(r,
					PERMISSIONSET_HAS_PERMISSION));
			return ps;
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public Collection<PermissionSet> getAllPermissionSets() {
		List<PermissionSet> list = new ArrayList<PermissionSet>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			ClosableIterator<Statement> stmtIt = getOntModel().listStatements(
					null, RDF.type, PERMISSIONSET);
			try {
				while (stmtIt.hasNext()) {
					Statement stmt = stmtIt.next();
					OntResource r = stmt.getSubject().as(OntResource.class);

					PermissionSet ps = new PermissionSet();
					ps.setUri(r.getURI());
					ps.setLabel(getPropertyStringValue(r, RDFS.label));
					ps.setPermissionUris(getPropertyResourceURIValues(r,
							PERMISSIONSET_HAS_PERMISSION));
					list.add(ps);
				}
			} finally {
				stmtIt.close();
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}

		Collections.sort(list, new PermissionSetsByUri());

		return list;
	}

	private String getUnusedURI() throws InsertException {
		String errMsg = null;

		String namespace = DEFAULT_NAMESPACE;
		String uri = null;

		Random random = new Random(System.currentTimeMillis());
		for (int attempts = 0; attempts < 30; attempts++) {
			int upperBound = (int) Math.pow(2, attempts + 13);
			uri = namespace + ("n" + random.nextInt(upperBound));
			errMsg = getWebappDaoFactory().checkURI(uri);
			if (errMsg == null) {
				return uri;
			}
		}

		throw new InsertException("Could not create URI for individual: "
				+ errMsg);
	}

	/**
	 * Since there is no reasoner on the UserAccountModel, this will return a
	 * false negative for a subtype of the specified type.
	 * 
	 * There should already be a lock on the model when this is called.
	 */
	private boolean isResourceOfType(OntResource r, OntClass type) {
		if (r == null) {
			return false;
		}
		if (type == null) {
			return false;
		}
		
		StmtIterator stmts = getOntModel().listStatements(r, RDF.type, type);
		if (stmts.hasNext()) {
			stmts.close();
			return true;
		} else {
			stmts.close();
			return false;
		}
	}

	private static class PermissionSetsByUri implements
			Comparator<PermissionSet> {
		@Override
		public int compare(PermissionSet ps1, PermissionSet ps2) {
			return ps1.getUri().compareTo(ps2.getUri());
		}
	}

}
