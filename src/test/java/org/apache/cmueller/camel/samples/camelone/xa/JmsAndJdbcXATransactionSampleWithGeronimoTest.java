package org.apache.cmueller.camel.samples.camelone.xa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.NamedXAResourceFactory;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JmsAndJdbcXATransactionSampleWithGeronimoTest extends BaseJmsAndJdbcXATransactionSampleTest {

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/JmsAndJdbcXATransactionSampleWithGeronimoTest-context.xml");
    }

    public static class NamedXADataSource implements XADataSource, NamedXAResourceFactory {
        private final XADataSource delegate;
        private final String name;

        public NamedXADataSource(RecoverableTransactionManager recoverableTransactionManager, XADataSource delegate, String name) {
            this.delegate = delegate;
            this.name = name;
            recoverableTransactionManager.registerNamedXAResourceFactory(this);
        }

        @Override
        public XAConnection getXAConnection() throws SQLException {
            return new NamedXAConnection(delegate.getXAConnection(), name);
        }

        @Override
        public XAConnection getXAConnection(String username, String password) throws SQLException {
            return new NamedXAConnection(delegate.getXAConnection(username, password), name);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return delegate.getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            delegate.setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            delegate.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return delegate.getLoginTimeout();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public NamedXAResource getNamedXAResource() throws SystemException {
            try {
                return (NamedXAResource) getXAConnection().getXAResource();
            } catch (SQLException e) {
                throw (SystemException) new SystemException().initCause(e);
            }
        }

        @Override
        public void returnNamedXAResource(NamedXAResource namedXAResource) {
            try {
                ((WrapperNamedXAResource) namedXAResource).getConnection().close();
            } catch (SQLException e) {
                // Ignore
            }
        }

        public static class NamedXAConnection implements XAConnection {
            private final XAConnection delegate;
            private final String name;

            public NamedXAConnection(XAConnection delegate, String name) {
                this.delegate = delegate;
                this.name = name;
            }

            public String getName() {
                return name;
            }

            @Override
            public XAResource getXAResource() throws SQLException {
                return new WrapperNamedXAResource(delegate.getXAResource(), this);
            }

            @Override
            public Connection getConnection() throws SQLException {
                return delegate.getConnection();
            }

            @Override
            public void close() throws SQLException {
                delegate.close();
            }

            @Override
            public void addConnectionEventListener(ConnectionEventListener listener) {
                delegate.addConnectionEventListener(listener);
            }

            @Override
            public void removeConnectionEventListener(ConnectionEventListener listener) {
                delegate.removeConnectionEventListener(listener);
            }

            @Override
            public void addStatementEventListener(StatementEventListener listener) {
                delegate.addStatementEventListener(listener);
            }

            @Override
            public void removeStatementEventListener(StatementEventListener listener) {
                delegate.removeStatementEventListener(listener);
            }
        }

        public static class WrapperNamedXAResource implements NamedXAResource {

            private final XAResource resource;
            private final NamedXAConnection connection;

            public WrapperNamedXAResource(XAResource resource, NamedXAConnection connection) {
                this.resource = resource;
                this.connection = connection;
            }

            @Override
            public String getName() {
                return connection.getName();
            }

            public NamedXAConnection getConnection() {
                return connection;
            }

            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                resource.commit(xid, b);
            }

            @Override
            public void end(Xid xid, int i) throws XAException {
                resource.end(xid, i);
            }

            @Override
            public void forget(Xid xid) throws XAException {
                resource.forget(xid);
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return resource.getTransactionTimeout();
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return resource.isSameRM(xaResource);
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return resource.prepare(xid);
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return resource.recover(i);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource.rollback(xid);
            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return resource.setTransactionTimeout(i);
            }

            @Override
            public void start(Xid xid, int i) throws XAException {
                resource.start(xid, i);
            }
        }
    }
}