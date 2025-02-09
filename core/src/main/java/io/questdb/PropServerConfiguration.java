/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.CairoSecurityContext;
import io.questdb.cairo.CommitMode;
import io.questdb.cairo.security.AllowAllCairoSecurityContext;
import io.questdb.cutlass.http.HttpServerConfiguration;
import io.questdb.cutlass.http.MimeTypesCache;
import io.questdb.cutlass.http.processors.JsonQueryProcessorConfiguration;
import io.questdb.cutlass.http.processors.StaticContentProcessorConfiguration;
import io.questdb.cutlass.json.JsonException;
import io.questdb.cutlass.json.JsonLexer;
import io.questdb.cutlass.line.*;
import io.questdb.cutlass.line.tcp.LineTcpReceiverConfiguration;
import io.questdb.cutlass.line.udp.LineUdpReceiverConfiguration;
import io.questdb.cutlass.pgwire.PGWireConfiguration;
import io.questdb.cutlass.text.TextConfiguration;
import io.questdb.cutlass.text.types.InputFormatConfiguration;
import io.questdb.log.Log;
import io.questdb.mp.WorkerPoolConfiguration;
import io.questdb.network.*;
import io.questdb.std.*;
import io.questdb.std.microtime.DateFormatCompiler;
import io.questdb.std.microtime.*;
import io.questdb.std.str.Path;
import io.questdb.std.time.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class PropServerConfiguration implements ServerConfiguration {
    public static final String CONFIG_DIRECTORY = "conf";
    private final IODispatcherConfiguration httpIODispatcherConfiguration = new HttpIODispatcherConfiguration();
    private final StaticContentProcessorConfiguration staticContentProcessorConfiguration = new PropStaticContentProcessorConfiguration();
    private final HttpServerConfiguration httpServerConfiguration = new PropHttpServerConfiguration();
    private final TextConfiguration textConfiguration = new PropTextConfiguration();
    private final CairoConfiguration cairoConfiguration = new PropCairoConfiguration();
    private final LineUdpReceiverConfiguration lineUdpReceiverConfiguration = new PropLineUdpReceiverConfiguration();
    private final JsonQueryProcessorConfiguration jsonQueryProcessorConfiguration = new PropJsonQueryProcessorConfiguration();
    private final TelemetryConfiguration telemetryConfiguration = new PropTelemetryConfiguration();
    private final int commitMode;
    private final boolean httpServerEnabled;
    private final int createAsSelectRetryCount;
    private final CharSequence defaultMapType;
    private final boolean defaultSymbolCacheFlag;
    private final int defaultSymbolCapacity;
    private final int fileOperationRetryCount;
    private final long idleCheckInterval;
    private final long inactiveReaderTTL;
    private final long inactiveWriterTTL;
    private final int indexValueBlockSize;
    private final int maxSwapFileCount;
    private final int mkdirMode;
    private final int parallelIndexThreshold;
    private final int readerPoolMaxSegments;
    private final long spinLockTimeoutUs;
    private final int sqlCacheRows;
    private final int sqlCacheBlocks;
    private final int sqlCharacterStoreCapacity;
    private final int sqlCharacterStoreSequencePoolCapacity;
    private final int sqlColumnPoolCapacity;
    private final int sqlCopyModelPoolCapacity;
    private final double sqlCompactMapLoadFactor;
    private final int sqlExpressionPoolCapacity;
    private final double sqlFastMapLoadFactor;
    private final int sqlJoinContextPoolCapacity;
    private final int sqlLexerPoolCapacity;
    private final int sqlMapKeyCapacity;
    private final int sqlMapPageSize;
    private final int sqlMapMaxPages;
    private final int sqlMapMaxResizes;
    private final int sqlModelPoolCapacity;
    private final long sqlSortKeyPageSize;
    private final int sqlSortKeyMaxPages;
    private final long sqlSortLightValuePageSize;
    private final int sqlSortLightValueMaxPages;
    private final int sqlHashJoinValuePageSize;
    private final int sqlHashJoinValueMaxPages;
    private final long sqlLatestByRowCount;
    private final int sqlHashJoinLightValuePageSize;
    private final int sqlHashJoinLightValueMaxPages;
    private final int sqlSortValuePageSize;
    private final int sqlSortValueMaxPages;
    private final long workStealTimeoutNanos;
    private final boolean parallelIndexingEnabled;
    private final int sqlJoinMetadataPageSize;
    private final int sqlJoinMetadataMaxResizes;
    private final int lineUdpCommitRate;
    private final int lineUdpGroupIPv4Address;
    private final int lineUdpMsgBufferSize;
    private final int lineUdpMsgCount;
    private final int lineUdpReceiveBufferSize;
    private final int lineUdpCommitMode;
    private final int[] sharedWorkerAffinity;
    private final int sharedWorkerCount;
    private final boolean sharedWorkerHaltOnError;
    private final WorkerPoolConfiguration workerPoolConfiguration = new PropWorkerPoolConfiguration();
    private final PGWireConfiguration pgWireConfiguration = new PropPGWireConfiguration();
    private final InputFormatConfiguration inputFormatConfiguration;
    private final LineProtoTimestampAdapter lineUdpTimestampAdapter;
    private final String inputRoot;
    private final boolean lineUdpEnabled;
    private final int lineUdpOwnThreadAffinity;
    private final boolean lineUdpUnicast;
    private final boolean lineUdpOwnThread;
    private final int sqlCopyBufferSize;
    private final long sqlAppendPageSize;
    private final int sqlAnalyticColumnPoolCapacity;
    private final int sqlCreateTableModelPoolCapacity;
    private final int sqlColumnCastModelPoolCapacity;
    private final int sqlRenameTableModelPoolCapacity;
    private final int sqlWithClauseModelPoolCapacity;
    private final int sqlInsertModelPoolCapacity;
    private final int sqlGroupByPoolCapacity;
    private final int sqlGroupByMapCapacity;
    private final int sqlMaxSymbolNotEqualsCount;
    private final DateLocale dateLocale;
    private final TimestampLocale timestampLocale;
    private final String backupRoot;
    private final TimestampFormat backupDirTimestampFormat;
    private final CharSequence backupTempDirName;
    private final int backupMkdirMode;
    private final int floatToStrCastScale;
    private final int doubleToStrCastScale;
    private final PropPGWireDispatcherConfiguration propPGWireDispatcherConfiguration = new PropPGWireDispatcherConfiguration();
    private final boolean pgEnabled;
    private final boolean telemetryEnabled;
    private final int telemetryQueueCapacity;
    private final LineTcpReceiverConfiguration lineTcpReceiverConfiguration = new PropLineTcpReceiverConfiguration();
    private final IODispatcherConfiguration lineTcpReceiverDispatcherConfiguration = new PropLineTcpReceiverIODispatcherConfiguration();
    private final boolean lineTcpEnabled;
    private final WorkerPoolAwareConfiguration lineTcpWorkerPoolConfiguration = new PropLineTcpWorkerPoolConfiguration();
    private boolean httpAllowDeflateBeforeSend;
    private int[] httpWorkerAffinity;
    private int connectionPoolInitialCapacity;
    private int connectionStringPoolCapacity;
    private int multipartHeaderBufferSize;
    private long multipartIdleSpinCount;
    private int recvBufferSize;
    private int requestHeaderBufferSize;
    private int responseHeaderBufferSize;
    private int httpWorkerCount;
    private boolean httpWorkerHaltOnError;
    private boolean httpServerKeepAlive;
    private int sendBufferSize;
    private CharSequence indexFileName;
    private String publicDirectory;
    private int activeConnectionLimit;
    private int eventCapacity;
    private int ioQueueCapacity;
    private long idleConnectionTimeout;
    private int interestQueueCapacity;
    private int listenBacklog;
    private int sndBufSize;
    private int rcvBufSize;
    private int dateAdapterPoolCapacity;
    private int jsonCacheLimit;
    private int jsonCacheSize;
    private double maxRequiredDelimiterStdDev;
    private double maxRequiredLineLengthStdDev;
    private int metadataStringPoolCapacity;
    private int rollBufferLimit;
    private int rollBufferSize;
    private int textAnalysisMaxLines;
    private int textLexerStringPoolCapacity;
    private int timestampAdapterPoolCapacity;
    private int utf8SinkSize;
    private MimeTypesCache mimeTypesCache;
    private String databaseRoot;
    private String keepAliveHeader;
    private int bindIPv4Address;
    private int bindPort;
    private int lineUdpBindIPV4Address;
    private int lineUdpPort;
    private int jsonQueryFloatScale;
    private int jsonQueryDoubleScale;
    private int jsonQueryConnectionCheckFrequency;
    private boolean httpFrozenClock;
    private boolean readOnlySecurityContext;
    private long maxHttpQueryResponseRowLimit;
    private boolean interruptOnClosedConnection;
    private int interruptorNIterationsPerCheck;
    private int interruptorBufferSize;
    private int pgNetActiveConnectionLimit;
    private int pgNetBindIPv4Address;
    private int pgNetBindPort;
    private int pgNetEventCapacity;
    private int pgNetIOQueueCapacity;
    private long pgNetIdleConnectionTimeout;
    private int pgNetInterestQueueCapacity;
    private int pgNetListenBacklog;
    private int pgNetRcvBufSize;
    private int pgNetSndBufSize;
    private int pgCharacterStoreCapacity;
    private int pgCharacterStorePoolCapacity;
    private int pgConnectionPoolInitialCapacity;
    private String pgPassword;
    private String pgUsername;
    private int pgFactoryCacheColumnCount;
    private int pgFactoryCacheRowCount;
    private int pgIdleRecvCountBeforeGivingUp;
    private int pgIdleSendCountBeforeGivingUp;
    private int pgMaxBlobSizeOnQuery;
    private int pgRecvBufferSize;
    private int pgSendBufferSize;
    private DateLocale pgDefaultDateLocale;
    private TimestampLocale pgDefaultTimestampLocale;
    private int[] pgWorkerAffinity;
    private int pgWorkerCount;
    private boolean pgHaltOnError;
    private boolean pgDaemonPool;
    private int lineTcpNetActiveConnectionLimit;
    private int lineTcpNetBindIPv4Address;
    private int lineTcpNetBindPort;
    private int lineTcpNetEventCapacity;
    private int lineTcpNetIOQueueCapacity;
    private long lineTcpNetIdleConnectionTimeout;
    private int lineTcpNetInterestQueueCapacity;
    private int lineTcpNetListenBacklog;
    private int lineTcpNetRcvBufSize;
    private int lineTcpConnectionPoolInitialCapacity;
    private LineProtoTimestampAdapter lineTcpTimestampAdapter;
    private int lineTcpMsgBufferSize;
    private int lineTcpMaxMeasurementSize;
    private int lineTcpWriterQueueSize;
    private int lineTcpWorkerCount;
    private int[] lineTcpWorkerAffinity;
    private boolean lineTcpWorkerPoolHaltOnError;
    private int lineTcpNUpdatesPerLoadRebalance;
    private double lineTcpMaxLoadRatio;
    private int lineTcpMaxUncommittedRows;
    private long lineTcpMaintenanceJobHysteresisInMs;
    private String lineTcpAuthDbPath;
    private String httpVersion;
    private final Log log;

    public PropServerConfiguration(
            String root,
            Properties properties,
            @Nullable Map<String, String> env,
            Log log
    ) throws ServerConfigurationException, JsonException {
        this.log = log;
        this.sharedWorkerCount = getInt(properties, env, "shared.worker.count", Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        this.sharedWorkerAffinity = getAffinity(properties, env, "shared.worker.affinity", sharedWorkerCount);
        this.sharedWorkerHaltOnError = getBoolean(properties, env, "shared.worker.haltOnError", false);
        this.httpServerEnabled = getBoolean(properties, env, "http.enabled", true);
        if (httpServerEnabled) {
            this.connectionPoolInitialCapacity = getInt(properties, env, "http.connection.pool.initial.capacity", 16);
            this.connectionStringPoolCapacity = getInt(properties, env, "http.connection.string.pool.capacity", 128);
            this.multipartHeaderBufferSize = getIntSize(properties, env, "http.multipart.header.buffer.size", 512);
            this.multipartIdleSpinCount = getLong(properties, env, "http.multipart.idle.spin.count", 10_000);
            this.recvBufferSize = getIntSize(properties, env, "http.receive.buffer.size", 1024 * 1024);
            this.requestHeaderBufferSize = getIntSize(properties, env, "http.request.header.buffer.size", 32 * 2014);
            this.responseHeaderBufferSize = getIntSize(properties, env, "http.response.header.buffer.size", 32 * 1024);
            this.httpWorkerCount = getInt(properties, env, "http.worker.count", 0);
            this.httpWorkerAffinity = getAffinity(properties, env, "http.worker.affinity", httpWorkerCount);
            this.httpWorkerHaltOnError = getBoolean(properties, env, "http.worker.haltOnError", false);
            this.sendBufferSize = getIntSize(properties, env, "http.send.buffer.size", 2 * 1024 * 1024);
            this.indexFileName = getString(properties, env, "http.static.index.file.name", "index.html");
            this.httpFrozenClock = getBoolean(properties, env, "http.frozen.clock", false);
            this.httpAllowDeflateBeforeSend = getBoolean(properties, env, "http.allow.deflate.before.send", false);
            this.httpServerKeepAlive = getBoolean(properties, env, "http.server.keep.alive", true);
            this.httpVersion = getString(properties, env, "http.version", "HTTP/1.1");
            if (!httpVersion.endsWith(" ")) {
                httpVersion += ' ';
            }

            int keepAliveTimeout = getInt(properties, env, "http.keep-alive.timeout", 5);
            int keepAliveMax = getInt(properties, env, "http.keep-alive.max", 10_000);

            if (keepAliveTimeout > 0 && keepAliveMax > 0) {
                this.keepAliveHeader = "Keep-Alive: timeout=" + keepAliveTimeout + ", max=" + keepAliveMax + Misc.EOL;
            } else {
                this.keepAliveHeader = null;
            }

            final String publicDirectory = getString(properties, env, "http.static.pubic.directory", "public");
            // translate public directory into absolute path
            // this will generate some garbage, but this is ok - we just doing this once on startup
            if (new File(publicDirectory).isAbsolute()) {
                this.publicDirectory = publicDirectory;
            } else {
                this.publicDirectory = new File(root, publicDirectory).getAbsolutePath();
            }

            final String databaseRoot = getString(properties, env, "cairo.root", "db");
            if (new File(databaseRoot).isAbsolute()) {
                this.databaseRoot = databaseRoot;
            } else {
                this.databaseRoot = new File(root, databaseRoot).getAbsolutePath();
            }

            this.activeConnectionLimit = getInt(properties, env, "http.net.active.connection.limit", 256);
            this.eventCapacity = getInt(properties, env, "http.net.event.capacity", 1024);
            this.ioQueueCapacity = getInt(properties, env, "http.net.io.queue.capacity", 1024);
            this.idleConnectionTimeout = getLong(properties, env, "http.net.idle.connection.timeout", 5 * 60 * 1000L);
            this.interestQueueCapacity = getInt(properties, env, "http.net.interest.queue.capacity", 1024);
            this.listenBacklog = getInt(properties, env, "http.net.listen.backlog", 256);
            this.sndBufSize = getIntSize(properties, env, "http.net.snd.buf.size", 2 * 1024 * 1024);
            this.rcvBufSize = getIntSize(properties, env, "http.net.rcv.buf.size", 2 * 1024 * 1024);
            this.dateAdapterPoolCapacity = getInt(properties, env, "http.text.date.adapter.pool.capacity", 16);
            this.jsonCacheLimit = getIntSize(properties, env, "http.text.json.cache.limit", 16384);
            this.jsonCacheSize = getIntSize(properties, env, "http.text.json.cache.size", 8192);
            this.maxRequiredDelimiterStdDev = getDouble(properties, env, "http.text.max.required.delimiter.stddev", 0.1222d);
            this.maxRequiredLineLengthStdDev = getDouble(properties, env, "http.text.max.required.line.length.stddev", 0.8);
            this.metadataStringPoolCapacity = getInt(properties, env, "http.text.metadata.string.pool.capacity", 128);

            this.rollBufferLimit = getIntSize(properties, env, "http.text.roll.buffer.limit", 1024 * 4096);
            this.rollBufferSize = getIntSize(properties, env, "http.text.roll.buffer.size", 1024);
            this.textAnalysisMaxLines = getInt(properties, env, "http.text.analysis.max.lines", 1000);
            this.textLexerStringPoolCapacity = getInt(properties, env, "http.text.lexer.string.pool.capacity", 64);
            this.timestampAdapterPoolCapacity = getInt(properties, env, "http.text.timestamp.adapter.pool.capacity", 64);
            this.utf8SinkSize = getIntSize(properties, env, "http.text.utf8.sink.size", 4096);

            this.jsonQueryConnectionCheckFrequency = getInt(properties, env, "http.json.query.connection.check.frequency", 1_000_000);
            this.jsonQueryFloatScale = getInt(properties, env, "http.json.query.float.scale", 4);
            this.jsonQueryDoubleScale = getInt(properties, env, "http.json.query.double.scale", 12);
            this.readOnlySecurityContext = getBoolean(properties, env, "http.security.readonly", false);
            this.maxHttpQueryResponseRowLimit = getLong(properties, env, "http.security.max.response.rows", Long.MAX_VALUE);
            this.interruptOnClosedConnection = getBoolean(properties, env, "http.security.interrupt.on.closed.connection", true);
            this.interruptorNIterationsPerCheck = getInt(properties, env, "http.security.interruptor.iterations.per.check", 2_000_000);
            this.interruptorBufferSize = getInt(properties, env, "http.security.interruptor.buffer.size", 64);

            parseBindTo(properties, env, "http.bind.to", "0.0.0.0:9000", (a, p) -> {
                bindIPv4Address = a;
                bindPort = p;
            });

            // load mime types
            try (Path path = new Path().of(new File(new File(root, CONFIG_DIRECTORY), "mime.types").getAbsolutePath()).$()) {
                this.mimeTypesCache = new MimeTypesCache(FilesFacadeImpl.INSTANCE, path);
            }
        }
        this.pgEnabled = getBoolean(properties, env, "pg.enabled", true);
        if (pgEnabled) {
            pgNetActiveConnectionLimit = getInt(properties, env, "pg.net.active.connection.limit", 10);
            parseBindTo(properties, env, "pg.net.bind.to", "0.0.0.0:8812", (a, p) -> {
                pgNetBindIPv4Address = a;
                pgNetBindPort = p;
            });

            this.pgNetEventCapacity = getInt(properties, env, "pg.net.event.capacity", 1024);
            this.pgNetIOQueueCapacity = getInt(properties, env, "pg.net.io.queue.capacity", 1024);
            this.pgNetIdleConnectionTimeout = getLong(properties, env, "pg.net.idle.timeout", 300_000);
            this.pgNetInterestQueueCapacity = getInt(properties, env, "pg.net.interest.queue.capacity", 1024);
            this.pgNetListenBacklog = getInt(properties, env, "pg.net.listen.backlog", 50_000);
            this.pgNetRcvBufSize = getIntSize(properties, env, "pg.net.recv.buf.size", -1);
            this.pgNetSndBufSize = getIntSize(properties, env, "pg.net.send.buf.size", -1);
            this.pgCharacterStoreCapacity = getInt(properties, env, "pg.character.store.capacity", 4096);
            this.pgCharacterStorePoolCapacity = getInt(properties, env, "pg.character.store.pool.capacity", 64);
            this.pgConnectionPoolInitialCapacity = getInt(properties, env, "pg.connection.pool.capacity", 64);
            this.pgPassword = getString(properties, env, "pg.password", "quest");
            this.pgUsername = getString(properties, env, "pg.user", "admin");
            this.pgFactoryCacheColumnCount = getInt(properties, env, "pg.factory.cache.column.count", 16);
            this.pgFactoryCacheRowCount = getInt(properties, env, "pg.factory.cache.row.count", 16);
            this.pgIdleRecvCountBeforeGivingUp = getInt(properties, env, "pg.idle.recv.count.before.giving.up", 10_000);
            this.pgIdleSendCountBeforeGivingUp = getInt(properties, env, "pg.idle.send.count.before.giving.up", 10_000);
            this.pgMaxBlobSizeOnQuery = getIntSize(properties, env, "pg.max.blob.size.on.query", 512 * 1024);
            this.pgRecvBufferSize = getIntSize(properties, env, "pg.recv.buffer.size", 1024 * 1024);
            this.pgSendBufferSize = getIntSize(properties, env, "pg.send.buffer.size", 1024 * 1024);
            final String dateLocale = getString(properties, env, "pg.date.locale", "en");
            this.pgDefaultDateLocale = DateLocaleFactory.INSTANCE.getLocale(dateLocale);
            if (this.pgDefaultDateLocale == null) {
                throw new ServerConfigurationException("pg.date.locale", dateLocale);
            }
            final String timestampLocale = getString(properties, env, "pg.timestamp.locale", "en");
            this.pgDefaultTimestampLocale = TimestampLocaleFactory.INSTANCE.getLocale(timestampLocale);
            if (this.pgDefaultTimestampLocale == null) {
                throw new ServerConfigurationException("pg.timestamp.locale", dateLocale);
            }
            this.pgWorkerCount = getInt(properties, env, "pg.worker.count", 0);
            this.pgWorkerAffinity = getAffinity(properties, env, "pg.worker.affinity", pgWorkerCount);
            this.pgHaltOnError = getBoolean(properties, env, "pg.halt.on.error", false);
            this.pgDaemonPool = getBoolean(properties, env, "pg.daemon.pool", true);
        }

        this.commitMode = getCommitMode(properties, env, "cairo.commit.mode");
        this.createAsSelectRetryCount = getInt(properties, env, "cairo.create.as.select.retry.count", 5);
        this.defaultMapType = getString(properties, env, "cairo.default.map.type", "fast");
        this.defaultSymbolCacheFlag = getBoolean(properties, env, "cairo.default.symbol.cache.flag", true);
        this.defaultSymbolCapacity = getInt(properties, env, "cairo.default.symbol.capacity", 256);
        this.fileOperationRetryCount = getInt(properties, env, "cairo.file.operation.retry.count", 30);
        this.idleCheckInterval = getLong(properties, env, "cairo.idle.check.interval", 5 * 60 * 1000L);
        this.inactiveReaderTTL = getLong(properties, env, "cairo.inactive.reader.ttl", 120_000);
        this.inactiveWriterTTL = getLong(properties, env, "cairo.inactive.writer.ttl", 600_000);
        this.indexValueBlockSize = Numbers.ceilPow2(getIntSize(properties, env, "cairo.index.value.block.size", 256));
        this.maxSwapFileCount = getInt(properties, env, "cairo.max.swap.file.count", 30);
        this.mkdirMode = getInt(properties, env, "cairo.mkdir.mode", 509);
        this.parallelIndexThreshold = getInt(properties, env, "cairo.parallel.index.threshold", 100000);
        this.readerPoolMaxSegments = getInt(properties, env, "cairo.reader.pool.max.segments", 5);
        this.spinLockTimeoutUs = getLong(properties, env, "cairo.spin.lock.timeout", 1_000_000);
        this.sqlCacheRows = getInt(properties, env, "cairo.cache.rows", 16);
        this.sqlCacheBlocks = getIntSize(properties, env, "cairo.cache.blocks", 4);
        this.sqlCharacterStoreCapacity = getInt(properties, env, "cairo.character.store.capacity", 1024);
        this.sqlCharacterStoreSequencePoolCapacity = getInt(properties, env, "cairo.character.store.sequence.pool.capacity", 64);
        this.sqlColumnPoolCapacity = getInt(properties, env, "cairo.column.pool.capacity", 4096);
        this.sqlCompactMapLoadFactor = getDouble(properties, env, "cairo.compact.map.load.factor", 0.7);
        this.sqlExpressionPoolCapacity = getInt(properties, env, "cairo.expression.pool.capacity", 8192);
        this.sqlFastMapLoadFactor = getDouble(properties, env, "cairo.fast.map.load.factor", 0.5);
        this.sqlJoinContextPoolCapacity = getInt(properties, env, "cairo.sql.join.context.pool.capacity", 64);
        this.sqlLexerPoolCapacity = getInt(properties, env, "cairo.lexer.pool.capacity", 2048);
        this.sqlMapKeyCapacity = getInt(properties, env, "cairo.sql.map.key.capacity", 2048 * 1024);
        this.sqlMapPageSize = getIntSize(properties, env, "cairo.sql.map.page.size", 4 * 1024 * 1024);
        this.sqlMapMaxPages = getIntSize(properties, env, "cairo.sql.map.max.pages", Integer.MAX_VALUE);
        this.sqlMapMaxResizes = getIntSize(properties, env, "cairo.sql.map.max.resizes", Integer.MAX_VALUE);
        this.sqlModelPoolCapacity = getInt(properties, env, "cairo.model.pool.capacity", 1024);
        this.sqlSortKeyPageSize = getLongSize(properties, env, "cairo.sql.sort.key.page.size", 4 * 1024 * 1024);
        this.sqlSortKeyMaxPages = getIntSize(properties, env, "cairo.sql.sort.key.max.pages", Integer.MAX_VALUE);
        this.sqlSortLightValuePageSize = getLongSize(properties, env, "cairo.sql.sort.light.value.page.size", 1048576);
        this.sqlSortLightValueMaxPages = getIntSize(properties, env, "cairo.sql.sort.light.value.max.pages", Integer.MAX_VALUE);
        this.sqlHashJoinValuePageSize = getIntSize(properties, env, "cairo.sql.hash.join.value.page.size", 16777216);
        this.sqlHashJoinValueMaxPages = getIntSize(properties, env, "cairo.sql.hash.join.value.max.pages", Integer.MAX_VALUE);
        this.sqlLatestByRowCount = getInt(properties, env, "cairo.sql.latest.by.row.count", 1000);
        this.sqlHashJoinLightValuePageSize = getIntSize(properties, env, "cairo.sql.hash.join.light.value.page.size", 1048576);
        this.sqlHashJoinLightValueMaxPages = getIntSize(properties, env, "cairo.sql.hash.join.light.value.max.pages", Integer.MAX_VALUE);
        this.sqlSortValuePageSize = getIntSize(properties, env, "cairo.sql.sort.value.page.size", 16777216);
        this.sqlSortValueMaxPages = getIntSize(properties, env, "cairo.sql.sort.value.max.pages", Integer.MAX_VALUE);
        this.workStealTimeoutNanos = getLong(properties, env, "cairo.work.steal.timeout.nanos", 10_000);
        this.parallelIndexingEnabled = getBoolean(properties, env, "cairo.parallel.indexing.enabled", true);
        this.sqlJoinMetadataPageSize = getIntSize(properties, env, "cairo.sql.join.metadata.page.size", 16384);
        this.sqlJoinMetadataMaxResizes = getIntSize(properties, env, "cairo.sql.join.metadata.max.resizes", Integer.MAX_VALUE);
        this.sqlAnalyticColumnPoolCapacity = getInt(properties, env, "cairo.sql.analytic.column.pool.capacity", 64);
        this.sqlCreateTableModelPoolCapacity = getInt(properties, env, "cairo.sql.create.table.model.pool.capacity", 16);
        this.sqlColumnCastModelPoolCapacity = getInt(properties, env, "cairo.sql.column.cast.model.pool.capacity", 16);
        this.sqlRenameTableModelPoolCapacity = getInt(properties, env, "cairo.sql.rename.table.model.pool.capacity", 16);
        this.sqlWithClauseModelPoolCapacity = getInt(properties, env, "cairo.sql.with.clause.model.pool.capacity", 128);
        this.sqlInsertModelPoolCapacity = getInt(properties, env, "cairo.sql.insert.model.pool.capacity", 64);
        this.sqlCopyModelPoolCapacity = getInt(properties, env, "cairo.sql.copy.model.pool.capacity", 32);
        this.sqlCopyBufferSize = getIntSize(properties, env, "cairo.sql.copy.buffer.size", 2 * 1024 * 1024);
        long sqlAppendPageSize = getLongSize(properties, env, "cairo.sql.append.page.size", 16 * 1024 * 1024);
        // round the append page size to the OS page size
        final long osPageSize = FilesFacadeImpl.INSTANCE.getPageSize();
        if ((sqlAppendPageSize % osPageSize) == 0) {
            this.sqlAppendPageSize = sqlAppendPageSize;
        } else {
            this.sqlAppendPageSize = (sqlAppendPageSize / osPageSize + 1) * osPageSize;
        }
        this.doubleToStrCastScale = getInt(properties, env, "cairo.sql.double.cast.scale", 12);
        this.floatToStrCastScale = getInt(properties, env, "cairo.sql.float.cast.scale", 4);
        this.sqlGroupByMapCapacity = getInt(properties, env, "cairo.sql.groupby.map.capacity", 1024);
        this.sqlGroupByPoolCapacity = getInt(properties, env, "cairo.sql.groupby.pool.capacity", 1024);
        this.sqlMaxSymbolNotEqualsCount = getInt(properties, env, "cairo.sql.max.symbol.not.equals.count", 100);
        final String sqlCopyFormatsFile = getString(properties, env, "cairo.sql.copy.formats.file", "/text_loader.json");
        this.telemetryEnabled = getBoolean(properties, env, "telemetry.enabled", true);
        this.telemetryQueueCapacity = getInt(properties, env, "telemetry.queue.capacity", 512);

        final String dateLocale = getString(properties, env, "cairo.date.locale", "en");
        this.dateLocale = DateLocaleFactory.INSTANCE.getLocale(dateLocale);
        if (this.dateLocale == null) {
            throw new ServerConfigurationException("cairo.date.locale", dateLocale);
        }

        final String timestampLocale = getString(properties, env, "cairo.timestamp.locale", "en");
        this.timestampLocale = TimestampLocaleFactory.INSTANCE.getLocale(timestampLocale);
        if (this.timestampLocale == null) {
            throw new ServerConfigurationException("cairo.timestamp.locale", timestampLocale);
        }

        this.inputFormatConfiguration = new InputFormatConfiguration(
                new DateFormatFactory(),
                DateLocaleFactory.INSTANCE,
                new TimestampFormatFactory(),
                TimestampLocaleFactory.INSTANCE,
                this.dateLocale,
                this.timestampLocale);

        try (JsonLexer lexer = new JsonLexer(1024, 1024)) {
            inputFormatConfiguration.parseConfiguration(lexer, sqlCopyFormatsFile);
        }

        this.inputRoot = getString(properties, env, "cairo.sql.copy.root", null);
        this.backupRoot = getString(properties, env, "cairo.sql.backup.root", null);
        this.backupDirTimestampFormat = getTimestampFormat(properties, env, "cairo.sql.backup.dir.datetime.format", "yyyy-MM-dd");
        this.backupTempDirName = getString(properties, env, "cairo.sql.backup.dir.tmp.name", "tmp");
        this.backupMkdirMode = getInt(properties, env, "cairo.sql.backup.mkdir.mode", 509);

        parseBindTo(properties, env, "line.udp.bind.to", "0.0.0.0:9009", (a, p) -> {
            this.lineUdpBindIPV4Address = a;
            this.lineUdpPort = p;
        });

        this.lineUdpGroupIPv4Address = getIPv4Address(properties, env, "line.udp.join", "232.1.2.3");
        this.lineUdpCommitRate = getInt(properties, env, "line.udp.commit.rate", 1_000_000);
        this.lineUdpMsgBufferSize = getIntSize(properties, env, "line.udp.msg.buffer.size", 2048);
        this.lineUdpMsgCount = getInt(properties, env, "line.udp.msg.count", 10_000);
        this.lineUdpReceiveBufferSize = getIntSize(properties, env, "line.udp.receive.buffer.size", 8 * 1024 * 1024);
        this.lineUdpEnabled = getBoolean(properties, env, "line.udp.enabled", true);
        this.lineUdpOwnThreadAffinity = getInt(properties, env, "line.udp.own.thread.affinity", -1);
        this.lineUdpOwnThread = getBoolean(properties, env, "line.udp.own.thread", false);
        this.lineUdpUnicast = getBoolean(properties, env, "line.udp.unicast", false);
        this.lineUdpCommitMode = getCommitMode(properties, env, "line.udp.commit.mode");
        this.lineUdpTimestampAdapter = getLineTimestampAdaptor(properties, env, "line.udp.timestamp");

        this.lineTcpEnabled = getBoolean(properties, env, "line.tcp.enabled", true);
        if (lineTcpEnabled) {
            lineTcpNetActiveConnectionLimit = getInt(properties, env, "line.tcp.net.active.connection.limit", 10);
            parseBindTo(properties, env, "line.tcp.net.bind.to", "0.0.0.0:9009", (a, p) -> {
                lineTcpNetBindIPv4Address = a;
                lineTcpNetBindPort = p;
            });

            this.lineTcpNetEventCapacity = getInt(properties, env, "line.tcp.net.event.capacity", 1024);
            this.lineTcpNetIOQueueCapacity = getInt(properties, env, "line.tcp.net.io.queue.capacity", 1024);
            this.lineTcpNetIdleConnectionTimeout = getLong(properties, env, "line.tcp.net.idle.timeout", 0);
            this.lineTcpNetInterestQueueCapacity = getInt(properties, env, "line.tcp.net.interest.queue.capacity", 1024);
            this.lineTcpNetListenBacklog = getInt(properties, env, "line.tcp.net.listen.backlog", 50_000);
            this.lineTcpNetRcvBufSize = getIntSize(properties, env, "line.tcp.net.recv.buf.size", -1);
            this.lineTcpConnectionPoolInitialCapacity = getInt(properties, env, "line.tcp.connection.pool.capacity", 64);
            this.lineTcpTimestampAdapter = getLineTimestampAdaptor(properties, env, "line.tcp.timestamp");
            this.lineTcpMsgBufferSize = getIntSize(properties, env, "line.tcp.msg.buffer.size", 2048);
            this.lineTcpMaxMeasurementSize = getIntSize(properties, env, "line.tcp.max.measurement.size", 2048);
            if (lineTcpMaxMeasurementSize > lineTcpMsgBufferSize) {
                throw new IllegalArgumentException(
                        "line.tcp.max.measurement.size (" + this.lineTcpMaxMeasurementSize + ") cannot be more than line.tcp.msg.buffer.size (" + this.lineTcpMsgBufferSize + ")");
            }
            this.lineTcpWriterQueueSize = getIntSize(properties, env, "line.tcp.writer.queue.size", 128);
            this.lineTcpWorkerCount = getInt(properties, env, "line.tcp.worker.count", 0);
            this.lineTcpWorkerAffinity = getAffinity(properties, env, "line.tcp.worker.affinity", lineTcpWorkerCount);
            this.lineTcpWorkerPoolHaltOnError = getBoolean(properties, env, "line.tcp.halt.on.error", false);
            this.lineTcpNUpdatesPerLoadRebalance = getInt(properties, env, "line.tcp.n.updates.per.load.balance", 10_000);
            this.lineTcpMaxLoadRatio = getDouble(properties, env, "line.tcp.max.load.ratio", 1.9);
            this.lineTcpMaxUncommittedRows = getInt(properties, env, "line.tcp.max.uncommitted.rows", 1000);
            this.lineTcpMaintenanceJobHysteresisInMs = getInt(properties, env, "line.tcp.maintenance.job.hysteresis.in.ms", 250);
            this.lineTcpAuthDbPath = getString(properties, env, "line.tcp.auth.db.path", null);
            if (null != lineTcpAuthDbPath) {
                this.lineTcpAuthDbPath = new File(root, this.lineTcpAuthDbPath).getAbsolutePath();
            }
        }
    }

    @Override
    public CairoConfiguration getCairoConfiguration() {
        return cairoConfiguration;
    }

    @Override
    public HttpServerConfiguration getHttpServerConfiguration() {
        return httpServerConfiguration;
    }

    @Override
    public LineUdpReceiverConfiguration getLineUdpReceiverConfiguration() {
        return lineUdpReceiverConfiguration;
    }

    @Override
    public LineTcpReceiverConfiguration getLineTcpReceiverConfiguration() {
        return lineTcpReceiverConfiguration;
    }

    @Override
    public WorkerPoolConfiguration getWorkerPoolConfiguration() {
        return workerPoolConfiguration;
    }

    @Override
    public PGWireConfiguration getPGWireConfiguration() {
        return pgWireConfiguration;
    }

    private int[] getAffinity(Properties properties, @Nullable Map<String, String> env, String key, int httpWorkerCount) throws ServerConfigurationException {
        final int[] result = new int[httpWorkerCount];
        String value = overrideWithEnv(properties, env, key);
        if (value == null) {
            Arrays.fill(result, -1);
        } else {
            String[] affinity = value.split(",");
            if (affinity.length != httpWorkerCount) {
                throw new ServerConfigurationException(key, "wrong number of affinity values");
            }
            for (int i = 0; i < httpWorkerCount; i++) {
                try {
                    result[i] = Numbers.parseInt(affinity[i]);
                } catch (NumericException e) {
                    throw new ServerConfigurationException(key, "Invalid affinity value: " + affinity[i]);
                }
            }
        }
        return result;
    }

    private boolean getBoolean(Properties properties, @Nullable Map<String, String> env, String key, boolean defaultValue) {
        final String value = overrideWithEnv(properties, env, key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private int getCommitMode(Properties properties, @Nullable Map<String, String> env, String key) {
        final String commitMode = overrideWithEnv(properties, env, key);

        if (commitMode == null) {
            return CommitMode.NOSYNC;
        }

        if (Chars.equalsLowerCaseAscii(commitMode, "nosync")) {
            return CommitMode.NOSYNC;
        }

        if (Chars.equalsLowerCaseAscii(commitMode, "async")) {
            return CommitMode.ASYNC;
        }

        if (Chars.equalsLowerCaseAscii(commitMode, "sync")) {
            return CommitMode.SYNC;
        }

        return CommitMode.NOSYNC;
    }

    private double getDouble(Properties properties, @Nullable Map<String, String> env, String key, double defaultValue) throws ServerConfigurationException {
        final String value = overrideWithEnv(properties, env, key);
        try {
            return value != null ? Numbers.parseDouble(value) : defaultValue;
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int getIPv4Address(Properties properties, Map<String, String> env, String key, String defaultValue) throws ServerConfigurationException {
        final String value = getString(properties, env, key, defaultValue);
        try {
            return Net.parseIPv4(value);
        } catch (NetworkError e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    private int getInt(Properties properties, @Nullable Map<String, String> env, String key, int defaultValue) throws ServerConfigurationException {
        final String value = overrideWithEnv(properties, env, key);
        try {
            return value != null ? Numbers.parseInt(value) : defaultValue;
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    private int getIntSize(Properties properties, @Nullable Map<String, String> env, String key, int defaultValue) throws ServerConfigurationException {
        final String value = overrideWithEnv(properties, env, key);
        try {
            return value != null ? Numbers.parseIntSize(value) : defaultValue;
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    private LineProtoTimestampAdapter getLineTimestampAdaptor(Properties properties, Map<String, String> env, String propNm) {
        final String lineUdpTimestampSwitch = getString(properties, env, propNm, "n");
        switch (lineUdpTimestampSwitch) {
            case "u":
                return LineProtoMicroTimestampAdapter.INSTANCE;
            case "ms":
                return LineProtoMilliTimestampAdapter.INSTANCE;
            case "s":
                return LineProtoSecondTimestampAdapter.INSTANCE;
            case "m":
                return LineProtoMinuteTimestampAdapter.INSTANCE;
            case "h":
                return LineProtoHourTimestampAdapter.INSTANCE;
            default:
                return LineProtoNanoTimestampAdapter.INSTANCE;
        }
    }

    private long getLong(Properties properties, @Nullable Map<String, String> env, String key, long defaultValue) throws ServerConfigurationException {
        final String value = overrideWithEnv(properties, env, key);
        try {
            return value != null ? Numbers.parseLong(value) : defaultValue;
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    private long getLongSize(Properties properties, @Nullable Map<String, String> env, String key, long defaultValue) throws ServerConfigurationException {
        final String value = overrideWithEnv(properties, env, key);
        try {
            return value != null ? Numbers.parseLongSize(value) : defaultValue;
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, value);
        }
    }

    private String getString(Properties properties, @Nullable Map<String, String> env, String key, String defaultValue) {
        String value = overrideWithEnv(properties, env, key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private TimestampFormat getTimestampFormat(Properties properties, @Nullable Map<String, String> env, String key, final String defaultPattern) {
        final String pattern = overrideWithEnv(properties, env, key);
        DateFormatCompiler compiler = new DateFormatCompiler();
        if (null != pattern) {
            return compiler.compile(pattern);
        }
        return compiler.compile(defaultPattern);
    }

    private String overrideWithEnv(Properties properties, @Nullable Map<String, String> env, String key) {
        String envCandidate = "QDB_" + key.replace('.', '_').toUpperCase();
        String envValue = env != null ? env.get(envCandidate) : null;
        if (envValue != null) {
            log.info().$("env config [key=").$(envCandidate).$(']').$();
            return envValue;
        }
        return properties.getProperty(key);
    }

    private void parseBindTo(
            Properties properties,
            Map<String, String> env,
            String key,
            String defaultValue,
            BindToParser parser
    ) throws ServerConfigurationException {

        final String bindTo = getString(properties, env, key, defaultValue);
        final int colonIndex = bindTo.indexOf(':');
        if (colonIndex == -1) {
            throw new ServerConfigurationException(key, bindTo);
        }

        final String ipv4Str = bindTo.substring(0, colonIndex);
        final int ipv4;
        try {
            ipv4 = Net.parseIPv4(ipv4Str);
        } catch (NetworkError e) {
            throw new ServerConfigurationException(key, ipv4Str);
        }

        final String portStr = bindTo.substring(colonIndex + 1);
        final int port;
        try {
            port = Numbers.parseInt(portStr);
        } catch (NumericException e) {
            throw new ServerConfigurationException(key, portStr);
        }

        parser.onReady(ipv4, port);
    }

    @FunctionalInterface
    private interface BindToParser {
        void onReady(int address, int port);
    }

    private class PropStaticContentProcessorConfiguration implements StaticContentProcessorConfiguration {
        @Override
        public FilesFacade getFilesFacade() {
            return FilesFacadeImpl.INSTANCE;
        }

        @Override
        public CharSequence getIndexFileName() {
            return indexFileName;
        }

        @Override
        public MimeTypesCache getMimeTypesCache() {
            return mimeTypesCache;
        }

        /**
         * Absolute path to HTTP public directory.
         *
         * @return path to public directory
         */
        @Override
        public CharSequence getPublicDirectory() {
            return publicDirectory;
        }

        @Override
        public String getKeepAliveHeader() {
            return keepAliveHeader;
        }
    }

    private class HttpIODispatcherConfiguration implements IODispatcherConfiguration {
        @Override
        public int getActiveConnectionLimit() {
            return activeConnectionLimit;
        }

        @Override
        public int getBindIPv4Address() {
            return bindIPv4Address;
        }

        @Override
        public int getBindPort() {
            return bindPort;
        }

        @Override
        public MillisecondClock getClock() {
            return MillisecondClockImpl.INSTANCE;
        }

        @Override
        public String getDispatcherLogName() {
            return "http-server";
        }

        @Override
        public EpollFacade getEpollFacade() {
            return EpollFacadeImpl.INSTANCE;
        }

        @Override
        public int getEventCapacity() {
            return eventCapacity;
        }

        @Override
        public int getIOQueueCapacity() {
            return ioQueueCapacity;
        }

        @Override
        public long getIdleConnectionTimeout() {
            return idleConnectionTimeout;
        }

        @Override
        public int getInitialBias() {
            return IOOperation.READ;
        }

        @Override
        public int getInterestQueueCapacity() {
            return interestQueueCapacity;
        }

        @Override
        public int getListenBacklog() {
            return listenBacklog;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getRcvBufSize() {
            return rcvBufSize;
        }

        @Override
        public SelectFacade getSelectFacade() {
            return SelectFacadeImpl.INSTANCE;
        }

        @Override
        public int getSndBufSize() {
            return sndBufSize;
        }
    }

    private class PropTextConfiguration implements TextConfiguration {

        @Override
        public int getDateAdapterPoolCapacity() {
            return dateAdapterPoolCapacity;
        }

        @Override
        public int getJsonCacheLimit() {
            return jsonCacheLimit;
        }

        @Override
        public int getJsonCacheSize() {
            return jsonCacheSize;
        }

        @Override
        public double getMaxRequiredDelimiterStdDev() {
            return maxRequiredDelimiterStdDev;
        }

        @Override
        public double getMaxRequiredLineLengthStdDev() {
            return maxRequiredLineLengthStdDev;
        }

        @Override
        public int getMetadataStringPoolCapacity() {
            return metadataStringPoolCapacity;
        }

        @Override
        public int getRollBufferLimit() {
            return rollBufferLimit;
        }

        @Override
        public int getRollBufferSize() {
            return rollBufferSize;
        }

        @Override
        public int getTextAnalysisMaxLines() {
            return textAnalysisMaxLines;
        }

        @Override
        public int getTextLexerStringPoolCapacity() {
            return textLexerStringPoolCapacity;
        }

        @Override
        public int getTimestampAdapterPoolCapacity() {
            return timestampAdapterPoolCapacity;
        }

        @Override
        public int getUtf8SinkSize() {
            return utf8SinkSize;
        }

        @Override
        public InputFormatConfiguration getInputFormatConfiguration() {
            return inputFormatConfiguration;
        }

        @Override
        public DateLocale getDefaultDateLocale() {
            return dateLocale;
        }

        @Override
        public TimestampLocale getDefaultTimestampLocale() {
            return timestampLocale;
        }
    }

    private class PropHttpServerConfiguration implements HttpServerConfiguration {

        @Override
        public int getConnectionPoolInitialCapacity() {
            return connectionPoolInitialCapacity;
        }

        @Override
        public int getConnectionStringPoolCapacity() {
            return connectionStringPoolCapacity;
        }

        @Override
        public int getMultipartHeaderBufferSize() {
            return multipartHeaderBufferSize;
        }

        @Override
        public long getMultipartIdleSpinCount() {
            return multipartIdleSpinCount;
        }

        @Override
        public int getRecvBufferSize() {
            return recvBufferSize;
        }

        @Override
        public int getRequestHeaderBufferSize() {
            return requestHeaderBufferSize;
        }

        @Override
        public int getResponseHeaderBufferSize() {
            return responseHeaderBufferSize;
        }

        @Override
        public int getQueryCacheBlocks() {
            return sqlCacheBlocks;
        }

        @Override
        public int getQueryCacheRows() {
            return sqlCacheRows;
        }

        @Override
        public MillisecondClock getClock() {
            return httpFrozenClock ? StationaryMillisClock.INSTANCE : MillisecondClockImpl.INSTANCE;
        }

        @Override
        public IODispatcherConfiguration getDispatcherConfiguration() {
            return httpIODispatcherConfiguration;
        }

        @Override
        public StaticContentProcessorConfiguration getStaticContentProcessorConfiguration() {
            return staticContentProcessorConfiguration;
        }

        @Override
        public JsonQueryProcessorConfiguration getJsonQueryProcessorConfiguration() {
            return jsonQueryProcessorConfiguration;
        }

        @Override
        public int getSendBufferSize() {
            return sendBufferSize;
        }

        @Override
        public boolean isEnabled() {
            return httpServerEnabled;
        }

        @Override
        public boolean getDumpNetworkTraffic() {
            return false;
        }

        @Override
        public boolean allowDeflateBeforeSend() {
            return httpAllowDeflateBeforeSend;
        }

        @Override
        public boolean readOnlySecurityContext() {
            return readOnlySecurityContext;
        }

        @Override
        public boolean isInterruptOnClosedConnection() {
            return interruptOnClosedConnection;
        }

        @Override
        public int getInterruptorNIterationsPerCheck() {
            return interruptorNIterationsPerCheck;
        }

        @Override
        public int getInterruptorBufferSize() {
            return interruptorBufferSize;
        }

        @Override
        public boolean getServerKeepAlive() {
            return httpServerKeepAlive;
        }

        @Override
        public String getHttpVersion() {
            return httpVersion;
        }

        @Override
        public int[] getWorkerAffinity() {
            return httpWorkerAffinity;
        }

        @Override
        public int getWorkerCount() {
            return httpWorkerCount;
        }

        @Override
        public boolean haltOnError() {
            return httpWorkerHaltOnError;
        }
    }

    private class PropCairoConfiguration implements CairoConfiguration {

        @Override
        public int getSqlCopyBufferSize() {
            return sqlCopyBufferSize;
        }

        @Override
        public int getCopyPoolCapacity() {
            return sqlCopyModelPoolCapacity;
        }

        @Override
        public int getCreateAsSelectRetryCount() {
            return createAsSelectRetryCount;
        }

        @Override
        public CharSequence getDefaultMapType() {
            return defaultMapType;
        }

        @Override
        public boolean getDefaultSymbolCacheFlag() {
            return defaultSymbolCacheFlag;
        }

        @Override
        public int getDefaultSymbolCapacity() {
            return defaultSymbolCapacity;
        }

        @Override
        public int getFileOperationRetryCount() {
            return fileOperationRetryCount;
        }

        @Override
        public FilesFacade getFilesFacade() {
            return FilesFacadeImpl.INSTANCE;
        }

        @Override
        public long getIdleCheckInterval() {
            return idleCheckInterval;
        }

        @Override
        public long getInactiveReaderTTL() {
            return inactiveReaderTTL;
        }

        @Override
        public long getInactiveWriterTTL() {
            return inactiveWriterTTL;
        }

        @Override
        public int getIndexValueBlockSize() {
            return indexValueBlockSize;
        }

        @Override
        public int getDoubleToStrCastScale() {
            return doubleToStrCastScale;
        }

        @Override
        public int getFloatToStrCastScale() {
            return floatToStrCastScale;
        }

        @Override
        public int getMaxSwapFileCount() {
            return maxSwapFileCount;
        }

        @Override
        public MicrosecondClock getMicrosecondClock() {
            return MicrosecondClockImpl.INSTANCE;
        }

        @Override
        public MillisecondClock getMillisecondClock() {
            return MillisecondClockImpl.INSTANCE;
        }

        @Override
        public NanosecondClock getNanosecondClock() {
            return NanosecondClockImpl.INSTANCE;
        }

        @Override
        public int getMkDirMode() {
            return mkdirMode;
        }

        @Override
        public int getParallelIndexThreshold() {
            return parallelIndexThreshold;
        }

        @Override
        public int getReaderPoolMaxSegments() {
            return readerPoolMaxSegments;
        }

        @Override
        public CharSequence getRoot() {
            return databaseRoot;
        }

        @Override
        public CharSequence getInputRoot() {
            return inputRoot;
        }

        @Override
        public CharSequence getBackupRoot() {
            return backupRoot;
        }

        @Override
        public TimestampFormat getBackupDirTimestampFormat() {
            return backupDirTimestampFormat;
        }

        @Override
        public CharSequence getBackupTempDirName() {
            return backupTempDirName;
        }

        @Override
        public int getBackupMkDirMode() {
            return backupMkdirMode;
        }

        @Override
        public long getSpinLockTimeoutUs() {
            return spinLockTimeoutUs;
        }

        @Override
        public int getSqlCharacterStoreCapacity() {
            return sqlCharacterStoreCapacity;
        }

        @Override
        public int getSqlCharacterStoreSequencePoolCapacity() {
            return sqlCharacterStoreSequencePoolCapacity;
        }

        @Override
        public int getSqlColumnPoolCapacity() {
            return sqlColumnPoolCapacity;
        }

        @Override
        public double getSqlCompactMapLoadFactor() {
            return sqlCompactMapLoadFactor;
        }

        @Override
        public int getSqlExpressionPoolCapacity() {
            return sqlExpressionPoolCapacity;
        }

        @Override
        public double getSqlFastMapLoadFactor() {
            return sqlFastMapLoadFactor;
        }

        @Override
        public int getSqlJoinContextPoolCapacity() {
            return sqlJoinContextPoolCapacity;
        }

        @Override
        public int getSqlLexerPoolCapacity() {
            return sqlLexerPoolCapacity;
        }

        @Override
        public int getSqlMapKeyCapacity() {
            return sqlMapKeyCapacity;
        }

        @Override
        public int getSqlMapPageSize() {
            return sqlMapPageSize;
        }

        @Override
        public int getSqlMapMaxPages() {
            return sqlMapMaxPages;
        }

        @Override
        public int getSqlMapMaxResizes() {
            return sqlMapMaxResizes;
        }

        @Override
        public int getSqlModelPoolCapacity() {
            return sqlModelPoolCapacity;
        }

        @Override
        public long getSqlSortKeyPageSize() {
            return sqlSortKeyPageSize;
        }

        @Override
        public int getSqlSortKeyMaxPages() {
            return sqlSortKeyMaxPages;
        }

        @Override
        public long getSqlSortLightValuePageSize() {
            return sqlSortLightValuePageSize;
        }

        @Override
        public int getSqlSortLightValueMaxPages() {
            return sqlSortLightValueMaxPages;
        }

        @Override
        public int getSqlHashJoinValuePageSize() {
            return sqlHashJoinValuePageSize;
        }

        @Override
        public int getSqlHashJoinValueMaxPages() {
            return sqlHashJoinValueMaxPages;
        }

        @Override
        public long getSqlLatestByRowCount() {
            return sqlLatestByRowCount;
        }

        @Override
        public int getSqlHashJoinLightValuePageSize() {
            return sqlHashJoinLightValuePageSize;
        }

        @Override
        public int getSqlHashJoinLightValueMaxPages() {
            return sqlHashJoinLightValueMaxPages;
        }

        @Override
        public int getSqlSortValuePageSize() {
            return sqlSortValuePageSize;
        }

        @Override
        public int getSqlSortValueMaxPages() {
            return sqlSortValueMaxPages;
        }

        @Override
        public TextConfiguration getTextConfiguration() {
            return textConfiguration;
        }

        @Override
        public long getWorkStealTimeoutNanos() {
            return workStealTimeoutNanos;
        }

        @Override
        public boolean isParallelIndexingEnabled() {
            return parallelIndexingEnabled;
        }

        @Override
        public int getSqlJoinMetadataPageSize() {
            return sqlJoinMetadataPageSize;
        }

        @Override
        public int getSqlJoinMetadataMaxResizes() {
            return sqlJoinMetadataMaxResizes;
        }

        @Override
        public int getAnalyticColumnPoolCapacity() {
            return sqlAnalyticColumnPoolCapacity;
        }

        @Override
        public int getCreateTableModelPoolCapacity() {
            return sqlCreateTableModelPoolCapacity;
        }

        @Override
        public int getColumnCastModelPoolCapacity() {
            return sqlColumnCastModelPoolCapacity;
        }

        @Override
        public int getRenameTableModelPoolCapacity() {
            return sqlRenameTableModelPoolCapacity;
        }

        @Override
        public int getWithClauseModelPoolCapacity() {
            return sqlWithClauseModelPoolCapacity;
        }

        @Override
        public int getInsertPoolCapacity() {
            return sqlInsertModelPoolCapacity;
        }

        @Override
        public int getCommitMode() {
            return commitMode;
        }

        @Override
        public DateLocale getDefaultDateLocale() {
            return dateLocale;
        }

        @Override
        public TimestampLocale getDefaultTimestampLocale() {
            return timestampLocale;
        }

        @Override
        public int getGroupByPoolCapacity() {
            return sqlGroupByPoolCapacity;
        }

        @Override
        public int getMaxSymbolNotEqualsCount() {
            return sqlMaxSymbolNotEqualsCount;
        }

        @Override
        public int getGroupByMapCapacity() {
            return sqlGroupByMapCapacity;
        }

        @Override
        public boolean enableTestFactories() {
            return false;
        }

        @Override
        public TelemetryConfiguration getTelemetryConfiguration() {
            return telemetryConfiguration;
        }

        @Override
        public long getAppendPageSize() {
            return sqlAppendPageSize;
        }
    }

    private class PropLineUdpReceiverConfiguration implements LineUdpReceiverConfiguration {
        @Override
        public int getCommitMode() {
            return lineUdpCommitMode;
        }

        @Override
        public int getBindIPv4Address() {
            return lineUdpBindIPV4Address;
        }

        @Override
        public int getCommitRate() {
            return lineUdpCommitRate;
        }

        @Override
        public int getGroupIPv4Address() {
            return lineUdpGroupIPv4Address;
        }

        @Override
        public int getMsgBufferSize() {
            return lineUdpMsgBufferSize;
        }

        @Override
        public int getMsgCount() {
            return lineUdpMsgCount;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getPort() {
            return lineUdpPort;
        }

        @Override
        public int getReceiveBufferSize() {
            return lineUdpReceiveBufferSize;
        }

        @Override
        public CairoSecurityContext getCairoSecurityContext() {
            return AllowAllCairoSecurityContext.INSTANCE;
        }

        @Override
        public boolean isEnabled() {
            return lineUdpEnabled;
        }

        @Override
        public boolean isUnicast() {
            return lineUdpUnicast;
        }

        @Override
        public boolean ownThread() {
            return lineUdpOwnThread;
        }

        @Override
        public int ownThreadAffinity() {
            return lineUdpOwnThreadAffinity;
        }

        @Override
        public LineProtoTimestampAdapter getTimestampAdapter() {
            return lineUdpTimestampAdapter;
        }
    }

    private class PropLineTcpReceiverIODispatcherConfiguration implements IODispatcherConfiguration {

        @Override
        public int getActiveConnectionLimit() {
            return lineTcpNetActiveConnectionLimit;
        }

        @Override
        public int getBindIPv4Address() {
            return lineTcpNetBindIPv4Address;
        }

        @Override
        public int getBindPort() {
            return lineTcpNetBindPort;
        }

        @Override
        public MillisecondClock getClock() {
            return MillisecondClockImpl.INSTANCE;
        }

        @Override
        public String getDispatcherLogName() {
            return "line-server";
        }

        @Override
        public EpollFacade getEpollFacade() {
            return EpollFacadeImpl.INSTANCE;
        }

        @Override
        public int getEventCapacity() {
            return lineTcpNetEventCapacity;
        }

        @Override
        public int getIOQueueCapacity() {
            return lineTcpNetIOQueueCapacity;
        }

        @Override
        public long getIdleConnectionTimeout() {
            return lineTcpNetIdleConnectionTimeout;
        }

        @Override
        public int getInitialBias() {
            return BIAS_READ;
        }

        @Override
        public int getInterestQueueCapacity() {
            return lineTcpNetInterestQueueCapacity;
        }

        @Override
        public int getListenBacklog() {
            return lineTcpNetListenBacklog;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getRcvBufSize() {
            return lineTcpNetRcvBufSize;
        }

        @Override
        public SelectFacade getSelectFacade() {
            return SelectFacadeImpl.INSTANCE;
        }

        @Override
        public int getSndBufSize() {
            return -1;
        }

    }

    private class PropLineTcpWorkerPoolConfiguration implements WorkerPoolAwareConfiguration {
        @Override
        public int[] getWorkerAffinity() {
            return lineTcpWorkerAffinity;
        }

        @Override
        public int getWorkerCount() {
            return lineTcpWorkerCount;
        }

        @Override
        public boolean haltOnError() {
            return lineTcpWorkerPoolHaltOnError;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private class PropLineTcpReceiverConfiguration implements LineTcpReceiverConfiguration {
        @Override
        public boolean isEnabled() {
            return lineTcpEnabled;
        }

        @Override
        public CairoSecurityContext getCairoSecurityContext() {
            return AllowAllCairoSecurityContext.INSTANCE;
        }

        @Override
        public LineProtoTimestampAdapter getTimestampAdapter() {
            return lineTcpTimestampAdapter;
        }

        @Override
        public int getConnectionPoolInitialCapacity() {
            return lineTcpConnectionPoolInitialCapacity;
        }

        @Override
        public IODispatcherConfiguration getNetDispatcherConfiguration() {
            return lineTcpReceiverDispatcherConfiguration;
        }

        @Override
        public int getNetMsgBufferSize() {
            return lineTcpMsgBufferSize;
        }

        @Override
        public int getMaxMeasurementSize() {
            return lineTcpMaxMeasurementSize;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getWriterQueueSize() {
            return lineTcpWriterQueueSize;
        }

        @Override
        public MicrosecondClock getMicrosecondClock() {
            return MicrosecondClockImpl.INSTANCE;
        }

        @Override
        public MillisecondClock getMillisecondClock() {
            return MillisecondClockImpl.INSTANCE;
        }

        @Override
        public WorkerPoolAwareConfiguration getWorkerPoolConfiguration() {
            return lineTcpWorkerPoolConfiguration;
        }

        @Override
        public int getNUpdatesPerLoadRebalance() {
            return lineTcpNUpdatesPerLoadRebalance;
        }

        @Override
        public double getMaxLoadRatio() {
            return lineTcpMaxLoadRatio;
        }

        @Override
        public int getMaxUncommittedRows() {
            return lineTcpMaxUncommittedRows;
        }

        @Override
        public long getMaintenanceJobHysteresisInMs() {
            return lineTcpMaintenanceJobHysteresisInMs;
        }

        @Override
        public String getAuthDbPath() {
            return lineTcpAuthDbPath;
        }
    }

    private class PropJsonQueryProcessorConfiguration implements JsonQueryProcessorConfiguration {
        @Override
        public MillisecondClock getClock() {
            return httpFrozenClock ? StationaryMillisClock.INSTANCE : MillisecondClockImpl.INSTANCE;
        }

        @Override
        public int getConnectionCheckFrequency() {
            return jsonQueryConnectionCheckFrequency;
        }

        @Override
        public FilesFacade getFilesFacade() {
            return FilesFacadeImpl.INSTANCE;
        }

        @Override
        public int getFloatScale() {
            return jsonQueryFloatScale;
        }

        @Override
        public int getDoubleScale() {
            return jsonQueryDoubleScale;
        }

        @Override
        public CharSequence getKeepAliveHeader() {
            return keepAliveHeader;
        }

        @Override
        public long getMaxQueryResponseRowLimit() {
            return maxHttpQueryResponseRowLimit;
        }
    }

    private class PropWorkerPoolConfiguration implements WorkerPoolConfiguration {
        @Override
        public int[] getWorkerAffinity() {
            return sharedWorkerAffinity;
        }

        @Override
        public int getWorkerCount() {
            return sharedWorkerCount;
        }

        @Override
        public boolean haltOnError() {
            return sharedWorkerHaltOnError;
        }
    }

    private class PropPGWireDispatcherConfiguration implements IODispatcherConfiguration {

        @Override
        public int getActiveConnectionLimit() {
            return pgNetActiveConnectionLimit;
        }

        @Override
        public int getBindIPv4Address() {
            return pgNetBindIPv4Address;
        }

        @Override
        public int getBindPort() {
            return pgNetBindPort;
        }

        @Override
        public MillisecondClock getClock() {
            return MillisecondClockImpl.INSTANCE;
        }

        @Override
        public String getDispatcherLogName() {
            return "pg-server";
        }

        @Override
        public EpollFacade getEpollFacade() {
            return EpollFacadeImpl.INSTANCE;
        }

        @Override
        public int getEventCapacity() {
            return pgNetEventCapacity;
        }

        @Override
        public int getIOQueueCapacity() {
            return pgNetIOQueueCapacity;
        }

        @Override
        public long getIdleConnectionTimeout() {
            return pgNetIdleConnectionTimeout;
        }

        @Override
        public int getInitialBias() {
            return BIAS_READ;
        }

        @Override
        public int getInterestQueueCapacity() {
            return pgNetInterestQueueCapacity;
        }

        @Override
        public int getListenBacklog() {
            return pgNetListenBacklog;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getRcvBufSize() {
            return pgNetRcvBufSize;
        }

        @Override
        public SelectFacade getSelectFacade() {
            return SelectFacadeImpl.INSTANCE;
        }

        @Override
        public int getSndBufSize() {
            return pgNetSndBufSize;
        }
    }

    private class PropPGWireConfiguration implements PGWireConfiguration {

        @Override
        public int getCharacterStoreCapacity() {
            return pgCharacterStoreCapacity;
        }

        @Override
        public int getCharacterStorePoolCapacity() {
            return pgCharacterStorePoolCapacity;
        }

        @Override
        public int getConnectionPoolInitialCapacity() {
            return pgConnectionPoolInitialCapacity;
        }

        @Override
        public String getDefaultPassword() {
            return pgPassword;
        }

        @Override
        public String getDefaultUsername() {
            return pgUsername;
        }

        @Override
        public IODispatcherConfiguration getDispatcherConfiguration() {
            return propPGWireDispatcherConfiguration;
        }

        @Override
        public boolean getDumpNetworkTraffic() {
            return false;
        }

        @Override
        public int getFactoryCacheColumnCount() {
            return pgFactoryCacheColumnCount;
        }

        @Override
        public int getFactoryCacheRowCount() {
            return pgFactoryCacheRowCount;
        }

        @Override
        public int getIdleRecvCountBeforeGivingUp() {
            return pgIdleRecvCountBeforeGivingUp;
        }

        @Override
        public int getIdleSendCountBeforeGivingUp() {
            return pgIdleSendCountBeforeGivingUp;
        }

        @Override
        public int getMaxBlobSizeOnQuery() {
            return pgMaxBlobSizeOnQuery;
        }

        @Override
        public NetworkFacade getNetworkFacade() {
            return NetworkFacadeImpl.INSTANCE;
        }

        @Override
        public int getRecvBufferSize() {
            return pgRecvBufferSize;
        }

        @Override
        public int getSendBufferSize() {
            return pgSendBufferSize;
        }

        @Override
        public String getServerVersion() {
            return "11.3";
        }

        @Override
        public DateLocale getDefaultDateLocale() {
            return pgDefaultDateLocale;
        }

        @Override
        public TimestampLocale getDefaultTimestampLocale() {
            return pgDefaultTimestampLocale;
        }

        @Override
        public int[] getWorkerAffinity() {
            return pgWorkerAffinity;
        }

        @Override
        public int getWorkerCount() {
            return pgWorkerCount;
        }

        @Override
        public boolean haltOnError() {
            return pgHaltOnError;
        }

        @Override
        public boolean isDaemonPool() {
            return pgDaemonPool;
        }

        @Override
        public boolean isEnabled() {
            return pgEnabled;
        }
    }

    private class PropTelemetryConfiguration implements TelemetryConfiguration {

        @Override
        public boolean getEnabled() {
            return telemetryEnabled;
        }

        @Override
        public int getQueueCapacity() {
            return telemetryQueueCapacity;
        }
    }
}
