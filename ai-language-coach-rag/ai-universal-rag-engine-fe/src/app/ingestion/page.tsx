'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useIngestContent, useLanguages, useIngestionHistory, usePipelineStats } from '@/lib/api';
import type { LanguageConfig, IngestionHistoryItem, PipelineStats as PipelineStatsType } from '@/types';

export default function IngestionPage() {
  const router = useRouter();
  const [ingestType, setIngestType] = useState<'url' | 'file'>('url');
  const [domain, setDomain] = useState('language-coach');
  const [targetLang, setTargetLang] = useState('czech');
  const [level, setLevel] = useState('a1');
  const [contentType, setContentType] = useState('grammar');
  const [sourceUrl, setSourceUrl] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [ingestResult, setIngestResult] = useState<{
    status: string;
    chunksCreated: number;
    documentId: string;
  } | null>(null);
  const [error, setError] = useState('');
  const [successDisplayed, setSuccessDisplayed] = useState(false);

  const { data: languages } = useLanguages();
  const { data: history, isLoading: historyLoading, refetch: refetchHistory } = useIngestionHistory();
  const { data: pipelineStats, isLoading: statsLoading } = usePipelineStats();
  const ingestMutation = useIngestContent();

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token) {
      router.push('/login');
    }
  }, [router]);

  const handleIngest = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIngestResult(null);
    setSuccessDisplayed(false);

    try {
      const formData = new FormData();
      formData.append('source_type', ingestType);

      if (ingestType === 'url') {
        formData.append('source_url', sourceUrl);
      } else if (selectedFile) {
        formData.append('file', selectedFile);
      }

      formData.append('domain', domain);
      formData.append('target_lang', targetLang);
      formData.append('level', level);
      formData.append('content_type', contentType);

      const result = await ingestMutation.mutateAsync(formData);
      setIngestResult(result);
      setSuccessDisplayed(true);
      refetchHistory();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Ingestion failed';
      setError(errorMessage);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const uniqueLanguages = languages?.reduce((acc: LanguageConfig[], lang) => {
    const existing = acc.find((l) => l.languageCode === lang.languageCode);
    if (!existing) {
      acc.push(lang);
    }
    return acc;
  }, []) || [];

  const formatDate = (timestamp: number) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="min-h-screen">
      <header className="mb-8">
        <h1 className="text-h1 font-h1 text-text-primary mb-2">Content Ingestion</h1>
        <p className="text-body font-body text-text-secondary">
          Expand your RAG knowledge base by importing web content or documents for linguistic analysis.
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div className="lg:col-span-8 space-y-6">
          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-4">1. Content Source</h3>
            <div className="flex p-1 bg-surface-container-low rounded-lg mb-6">
              <button
                type="button"
                onClick={() => setIngestType('url')}
                className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-md transition-colors ${
                  ingestType === 'url'
                    ? 'bg-white text-primary shadow-sm font-medium'
                    : 'text-text-secondary hover:text-text-primary font-medium'
                }`}
              >
                <span className="material-symbols-outlined text-[20px]">link</span>
                URL
              </button>
              <button
                type="button"
                onClick={() => setIngestType('file')}
                className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-md transition-colors ${
                  ingestType === 'file'
                    ? 'bg-white text-primary shadow-sm font-medium'
                    : 'text-text-secondary hover:text-text-primary font-medium'
                }`}
              >
                <span className="material-symbols-outlined text-[20px]">upload_file</span>
                File Upload
              </button>
            </div>

            {ingestType === 'url' ? (
              <div className="space-y-4">
                <div>
                  <label htmlFor="sourceUrl" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                    Target URL
                  </label>
                  <div className="relative">
                    <input
                      id="sourceUrl"
                      type="url"
                      value={sourceUrl}
                      onChange={(e) => setSourceUrl(e.target.value)}
                      className="w-full pl-4 pr-12 py-3 bg-surface-background border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                      placeholder="https://example.com/article-path"
                    />
                    <div className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary">
                      <span className="material-symbols-outlined">public</span>
                    </div>
                  </div>
                  <p className="text-[12px] text-text-secondary mt-2">Maximum recursive depth: 2 levels.</p>
                </div>
              </div>
            ) : (
              <div>
                <label htmlFor="file" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Upload File
                </label>
                <div className="border-2 border-dashed border-border-subtle rounded-lg p-6 text-center">
                  <span className="material-symbols-outlined text-4xl text-text-secondary mb-2">cloud_upload</span>
                  <input
                    id="file"
                    type="file"
                    onChange={handleFileChange}
                    className="hidden"
                    accept=".pdf,.doc,.docx,.txt,.md"
                  />
                  <label htmlFor="file" className="block cursor-pointer">
                    <span className="text-primary font-medium">Click to upload</span>
                    <span className="text-text-secondary"> or drag and drop</span>
                  </label>
                  <p className="text-[12px] text-text-secondary mt-2">PDF, DOCX, TXT, MD up to 10MB</p>
                </div>
                {selectedFile && (
                  <div className="mt-3 p-3 bg-surface-container-low rounded-lg flex items-center gap-3">
                    <span className="material-symbols-outlined text-primary">description</span>
                    <div className="flex-1">
                      <p className="text-sm text-text-primary font-medium">{selectedFile.name}</p>
                      <p className="text-[12px] text-text-secondary">
                        {(selectedFile.size / 1024).toFixed(2)} KB
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => setSelectedFile(null)}
                      className="text-text-secondary hover:text-error"
                    >
                      <span className="material-symbols-outlined">close</span>
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-6">2. Knowledge Metadata</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="domain" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Domain
                </label>
                <select
                  id="domain"
                  value={domain}
                  onChange={(e) => setDomain(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  <option value="language-coach">Language Coach</option>
                  <option value="business-economics">Business & Economics</option>
                  <option value="academic-scientific">Academic/Scientific</option>
                  <option value="casual-conversation">Casual Conversation</option>
                  <option value="technical-documentation">Technical Documentation</option>
                </select>
              </div>

              <div>
                <label htmlFor="targetLang" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Target Language
                </label>
                <select
                  id="targetLang"
                  value={targetLang}
                  onChange={(e) => setTargetLang(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  {uniqueLanguages.map((lang) => (
                    <option key={lang.languageCode} value={lang.languageCode.toLowerCase()}>
                      {lang.languageName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="level" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Proficiency Level
                </label>
                <select
                  id="level"
                  value={level}
                  onChange={(e) => setLevel(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  <option value="a1">A1 - Beginner</option>
                  <option value="a2">A2 - Elementary</option>
                  <option value="b1">B1 - Intermediate</option>
                  <option value="b2">B2 - Upper Intermediate</option>
                  <option value="c1">C1 - Advanced</option>
                  <option value="c2">C2 - Proficient</option>
                </select>
              </div>

              <div>
                <label htmlFor="contentType" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Content Type
                </label>
                <select
                  id="contentType"
                  value={contentType}
                  onChange={(e) => setContentType(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  <option value="news-article">News Article</option>
                  <option value="textbook-chapter">Textbook Chapter</option>
                  <option value="transcript">Transcript</option>
                  <option value="legal-text">Legal Text</option>
                  <option value="grammar">Grammar</option>
                  <option value="vocabulary">Vocabulary</option>
                  <option value="dialogue">Dialogue</option>
                </select>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-between pt-4">
            <div className="flex items-center gap-2 text-text-secondary">
              <span className="material-symbols-outlined text-[20px]">info</span>
              <span className="text-label-sm">System capacity: 82% available</span>
            </div>
            <button
              type="button"
              onClick={handleIngest}
              disabled={
                ingestMutation.isPending ||
                (ingestType === 'url' && !sourceUrl) ||
                (ingestType === 'file' && !selectedFile)
              }
              className="bg-primary text-white px-8 py-3 rounded-lg font-semibold hover:bg-primary-container transition-colors shadow-md active:scale-95 duration-150 flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span className="material-symbols-outlined">bolt</span>
              {ingestMutation.isPending ? 'Processing...' : 'Start Ingestion'}
            </button>
          </div>
        </div>

        <div className="lg:col-span-4 space-y-6">
          {successDisplayed && ingestResult && (
            <div className="bg-surface-container-low border border-border-subtle rounded-xl p-card-padding overflow-hidden relative">
              <div className="absolute -right-4 -top-4 opacity-5">
                <span
                  className="material-symbols-outlined text-[120px]"
                  style={{ fontVariationSettings: "'FILL' 1" }}
                >
                  check_circle
                </span>
              </div>
              <div className="flex items-start gap-3 mb-6 relative">
                <div className="bg-chart-green text-white p-2 rounded-full">
                  <span className="material-symbols-outlined text-[20px]" style={{ fontVariationSettings: "'FILL' 1" }}>
                    check
                  </span>
                </div>
                <div>
                  <h4 className="text-h2 font-h2 text-chart-green mb-1">Ingestion Successful</h4>
                  <p className="text-label-sm text-text-secondary">Knowledge base updated.</p>
                </div>
              </div>
              <div className="space-y-4 relative">
                <div className="p-3 bg-white border border-border-subtle rounded-lg">
                  <p className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">
                    Document ID
                  </p>
                  <p className="font-mono text-sm text-text-primary">{ingestResult.documentId}</p>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="p-3 bg-white border border-border-subtle rounded-lg">
                    <p className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Chunks</p>
                    <p className="font-stat-value text-stat-value text-primary">
                      {ingestResult.chunksCreated.toLocaleString()}
                    </p>
                  </div>
                  <div className="p-3 bg-white border border-border-subtle rounded-lg">
                    <p className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Status</p>
                    <p className="font-stat-value text-stat-value text-chart-green capitalize">{ingestResult.status}</p>
                  </div>
                </div>
                <div className="pt-2">
                  <button className="w-full py-2.5 border border-primary text-primary font-medium rounded-lg hover:bg-primary/5 transition-colors text-sm">
                    View Vector Map
                  </button>
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="bg-error-container border border-error-container rounded-xl p-card-padding">
              <div className="flex items-start gap-3">
                <div className="text-error">
                  <span className="material-symbols-outlined text-[20px]">error</span>
                </div>
                <div>
                  <h4 className="text-h2 font-h2 text-error mb-1">Ingestion Failed</h4>
                  <p className="text-label-sm text-on-error-container">{error}</p>
                </div>
              </div>
            </div>
          )}

          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-4">Pipeline Stats</h3>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-label-sm text-text-secondary">Encoding Latency</span>
                  <span className="text-label-sm font-bold text-text-primary">
                    {statsLoading ? '...' : `${pipelineStats?.encodingLatencyMs || 0}ms`}
                  </span>
                </div>
                <div className="w-full bg-surface-container-low h-1.5 rounded-full overflow-hidden">
                  <div 
                    className="bg-chart-blue h-full" 
                    style={{ width: `${Math.min(((pipelineStats?.encodingLatencyMs || 0) / 1000) * 100, 100)}%` }}
                  ></div>
                </div>
              </div>
              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-label-sm text-text-secondary">Embedding Load</span>
                  <span className="text-label-sm font-bold text-text-primary">
                    {statsLoading ? '...' : `${pipelineStats?.embeddingLoadPercent || 0}%`}
                  </span>
                </div>
                <div className="w-full bg-surface-container-low h-1.5 rounded-full overflow-hidden">
                  <div 
                    className="bg-chart-amber h-full" 
                    style={{ width: `${pipelineStats?.embeddingLoadPercent || 0}%` }}
                  ></div>
                </div>
              </div>
              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-label-sm text-text-secondary">Total Chunks</span>
                  <span className="text-label-sm font-bold text-text-primary">
                    {statsLoading ? '...' : pipelineStats?.totalChunksProcessed.toLocaleString() || '0'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-4">Recent Ingestions</h3>
            {historyLoading ? (
              <div className="text-label-sm text-text-secondary">Loading...</div>
            ) : history && history.length > 0 ? (
              <div className="space-y-3 max-h-64 overflow-y-auto">
                {(history as IngestionHistoryItem[]).slice(0, 5).map((item) => (
                  <div key={item.id} className="p-3 bg-surface-container-low rounded-lg">
                    <div className="flex justify-between items-start mb-1">
                      <span className="text-sm font-medium text-text-primary truncate">
                        {item.sourceUrl || item.fileName || 'URL Import'}
                      </span>
                      <span className={`text-[10px] px-2 py-0.5 rounded-full ${
                        item.status === 'success' 
                          ? 'bg-chart-green/10 text-chart-green' 
                          : item.status === 'pending'
                          ? 'bg-chart-amber/10 text-chart-amber'
                          : 'bg-chart-red/10 text-chart-red'
                      }`}>
                        {item.status}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-[10px] text-text-secondary">
                        {item.chunksCreated} chunks
                      </span>
                      <span className="text-[10px] text-text-secondary">
                        {formatDate(item.createdAt)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-label-sm text-text-secondary">
                No ingestion history yet. Start by importing content above.
              </div>
            )}
          </div>

          <div className="rounded-xl overflow-hidden h-40 relative group">
            <div className="w-full h-full bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center">
              <div className="absolute inset-0 flex items-center justify-center">
                <span className="bg-white/90 backdrop-blur px-4 py-2 rounded-full text-[12px] font-bold text-primary shadow-lg">
                  Network Health: Optimal
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}