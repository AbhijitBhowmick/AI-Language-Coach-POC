'use client';

import { useState, useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useRAGMetrics, parseMetrics } from '@/lib/api';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import type { ParsedMetrics } from '@/types';

const REFRESH_INTERVALS = [
  { value: 10000, label: '10 seconds' },
  { value: 30000, label: '30 seconds' },
  { value: 60000, label: '1 minute' },
  { value: 0, label: 'Disabled' },
];

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444'];

function CacheHitGauge({ ratio }: { ratio: number }) {
  const data = [
    { name: 'Hits', value: Math.round(ratio * 100) },
    { name: 'Misses', value: Math.round((1 - ratio) * 100) },
  ];

  return (
    <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
      <h3 className="text-h2 font-h2 text-text-primary mb-4">Cache Hit Ratio</h3>
      <div className="h-48">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={40}
              outerRadius={70}
              paddingAngle={5}
              dataKey="value"
              label={({ name, value }) => `${value}%`}
              labelLine={false}
            >
              {data.map((_, index) => (
                <Cell key={`cell-${index}`} fill={index === 0 ? COLORS[0] : COLORS[3]} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <div className="text-center mt-2">
        <span className="text-stat-value font-statValue text-primary">{(ratio * 100).toFixed(1)}%</span>
        <p className="text-label-sm text-text-secondary">Cache Efficiency</p>
      </div>
    </div>
  );
}

function LatencyChart({ metrics, history }: { metrics: ParsedMetrics; history: ParsedMetrics[] }) {
  const latencyData = useMemo(() => {
    const data = history.slice(-20).map((m, i) => ({
      time: i + 1,
      request: m.requestLatency * 1000,
      embedding: m.embeddingLatency * 1000,
      generation: m.generationLatency * 1000,
      vectorSearch: m.vectorSearchLatency * 1000,
    }));
    data.push({
      time: history.length + 1,
      request: metrics.requestLatency * 1000,
      embedding: metrics.embeddingLatency * 1000,
      generation: metrics.generationLatency * 1000,
      vectorSearch: metrics.vectorSearchLatency * 1000,
    });
    return data;
  }, [metrics, history]);

  return (
    <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
      <h3 className="text-h2 font-h2 text-text-primary mb-4">Latency (ms)</h3>
      <div className="h-64">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={latencyData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="time" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="request" stroke={COLORS[0]} strokeWidth={2} dot={false} name="Request" />
            <Line type="monotone" dataKey="embedding" stroke={COLORS[1]} strokeWidth={2} dot={false} name="Embedding" />
            <Line type="monotone" dataKey="generation" stroke={COLORS[2]} strokeWidth={2} dot={false} name="Generation" />
            <Line type="monotone" dataKey="vectorSearch" stroke={COLORS[3]} strokeWidth={2} dot={false} name="Vector Search" />
          </LineChart>
        </ResponsiveContainer>
      </div>
      <div className="flex flex-wrap gap-4 mt-4 justify-center">
        <div className="flex items-center">
          <div className="w-3 h-3 rounded-full bg-chart-blue mr-1"></div>
          <span className="text-label-sm text-text-secondary">Request</span>
        </div>
        <div className="flex items-center">
          <div className="w-3 h-3 rounded-full bg-chart-green mr-1"></div>
          <span className="text-label-sm text-text-secondary">Embedding</span>
        </div>
        <div className="flex items-center">
          <div className="w-3 h-3 rounded-full bg-chart-amber mr-1"></div>
          <span className="text-label-sm text-text-secondary">Generation</span>
        </div>
        <div className="flex items-center">
          <div className="w-3 h-3 rounded-full bg-chart-red mr-1"></div>
          <span className="text-label-sm text-text-secondary">Vector Search</span>
        </div>
      </div>
    </div>
  );
}

function RequestStatsWidget({ metrics }: { metrics: ParsedMetrics }) {
  const successRate = metrics.totalRequests > 0
    ? (metrics.successfulRequests / metrics.totalRequests) * 100
    : 100;

  return (
    <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
      <h3 className="text-h2 font-h2 text-text-primary mb-4">Request Statistics</h3>
      <dl className="grid grid-cols-2 gap-4">
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Total Requests</dt>
          <dd className="mt-1 text-stat-value font-statValue text-text-primary">{metrics.totalRequests}</dd>
        </div>
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Success Rate</dt>
          <dd className="mt-1 text-stat-value font-statValue text-chart-green">{successRate.toFixed(1)}%</dd>
        </div>
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Cache Hits</dt>
          <dd className="mt-1 text-stat-value font-statValue text-chart-blue">{metrics.cacheHits}</dd>
        </div>
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Cache Misses</dt>
          <dd className="mt-1 text-stat-value font-statValue text-chart-amber">{metrics.cacheMisses}</dd>
        </div>
      </dl>
    </div>
  );
}

function TokenUsageWidget({ metrics }: { metrics: ParsedMetrics }) {
  return (
    <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
      <h3 className="text-h2 font-h2 text-text-primary mb-4">Token Usage</h3>
      <dl className="grid grid-cols-2 gap-4">
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Prompt Tokens</dt>
          <dd className="mt-1 text-stat-value font-statValue text-text-primary">
            {metrics.promptTokens.toLocaleString()}
          </dd>
        </div>
        <div className="bg-surface-container-low rounded-lg p-4">
          <dt className="text-label-sm font-medium text-text-secondary">Completion Tokens</dt>
          <dd className="mt-1 text-stat-value font-statValue text-text-primary">
            {metrics.completionTokens.toLocaleString()}
          </dd>
        </div>
        <div className="bg-surface-container-low rounded-lg p-4 col-span-2">
          <dt className="text-label-sm font-medium text-text-secondary">Total Tokens</dt>
          <dd className="mt-1 text-stat-value font-statValue text-primary">
            {(metrics.promptTokens + metrics.completionTokens).toLocaleString()}
          </dd>
        </div>
      </dl>
    </div>
  );
}

export default function PerformancePage() {
  const router = useRouter();
  const [refreshInterval, setRefreshInterval] = useState(30000);
  const [history, setHistory] = useState<ParsedMetrics[]>([]);

  const { data: metricsText, isLoading, error, refetch } = useRAGMetrics(
    refreshInterval > 0 ? refreshInterval : undefined
  );

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token) {
      router.push('/login');
    }
  }, [router]);

  const metrics: ParsedMetrics = useMemo(() => {
    if (!metricsText) {
      return {
        cacheHitRatio: 0,
        cacheHits: 0,
        cacheMisses: 0,
        requestLatency: 0,
        embeddingLatency: 0,
        generationLatency: 0,
        vectorSearchLatency: 0,
        totalRequests: 0,
        successfulRequests: 0,
        failedRequests: 0,
        promptTokens: 0,
        completionTokens: 0,
      };
    }
    const parsed = parseMetrics(metricsText);
    setHistory((prev) => {
      const newHistory = [...prev, parsed].slice(-30);
      return newHistory;
    });
    return parsed;
  }, [metricsText]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto">
        <div className="bg-error-container border border-border-subtle rounded-xl p-card-padding">
          <div className="flex items-start gap-3">
            <div className="text-error">
              <span className="material-symbols-outlined text-[20px]">error</span>
            </div>
            <div>
              <h4 className="text-h2 font-h2 text-error mb-1">Failed to Load Metrics</h4>
              <p className="text-label-sm text-text-secondary mb-3">Is the RAG engine running?</p>
              <button
                onClick={() => refetch()}
                className="text-primary font-medium hover:underline"
              >
                Retry
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <header className="mb-8">
        <h1 className="text-h1 font-h1 text-text-primary mb-2">Analytics</h1>
        <p className="text-body font-body text-text-secondary">
          Monitor RAG engine performance metrics and usage statistics.
        </p>
      </header>

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-h2 font-h2 text-text-primary">RAG Performance Metrics</h2>
        <div className="flex items-center space-x-4">
          <label className="text-label-sm text-text-secondary">Refresh:</label>
          <select
            value={refreshInterval}
            onChange={(e) => setRefreshInterval(Number(e.target.value))}
            className="w-40 px-3 py-2 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none text-sm"
          >
            {REFRESH_INTERVALS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <button
            onClick={() => refetch()}
            className="px-4 py-2 border border-primary text-primary rounded-lg font-medium hover:bg-primary/5 transition-colors text-sm flex items-center gap-2"
          >
            <span className="material-symbols-outlined text-[18px]">refresh</span>
            Refresh
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <CacheHitGauge ratio={metrics.cacheHitRatio} />
        <RequestStatsWidget metrics={metrics} />
        <LatencyChart metrics={metrics} history={history} />
        <TokenUsageWidget metrics={metrics} />
      </div>

      <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
        <h3 className="text-h2 font-h2 text-text-primary mb-4">Raw Metrics (Prometheus Format)</h3>
        <pre className="bg-surface-container-low p-4 rounded-lg overflow-x-auto text-xs text-text-primary max-h-64 font-mono">
          {metricsText || 'No metrics available'}
        </pre>
      </div>
    </div>
  );
}