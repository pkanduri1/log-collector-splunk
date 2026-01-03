import { useState, useRef, useEffect } from 'react';

const EXAMPLE_PROMPTS = [
    "Why did the payment batch fail?",
    "Show me all errors from the last hour",
    "Summarize the root cause of 500 errors",
    "List unique users who failed login"
];

/**
 * ChatInterface Component
 * Renders the main chat UI for interacting with the Splunk Intelligent Layer.
 * Manages message state, input handling, and backend API communication.
 */
export default function ChatInterface() {
    const [messages, setMessages] = useState([
        { id: 1, type: 'bot', content: 'Hello! I am your Splunk AI Assistant. Ask me anything about your logs.' }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);

    /**
     * Scrolls the chat window to the bottom.
     * Triggered automatically when new messages are added.
     */
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(scrollToBottom, [messages]);

    /**
     * Handles the submission of a user message.
     * Sends the question to the backend and updates the UI with the response.
     * 
     * @param {Event} e - The form submission event.
     */
    const handleSubmit = async (e) => {
        e?.preventDefault();
        if (!input.trim() || isLoading) return;

        const userMessage = { id: Date.now(), type: 'user', content: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);

        try {
            const response = await fetch('http://localhost:8080/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question: userMessage.content }),
            });

            if (!response.ok) throw new Error('Network response was not ok');

            const data = await response.json();

            const botMessage = {
                id: Date.now() + 1,
                type: 'bot',
                content: data.aiSummary, // Updated to use aiSummary matching backend DTO
                spl: data.generatedSpl
            };

            setMessages(prev => [...prev, botMessage]);
        } catch (error) {
            console.error('Error:', error);
            setMessages(prev => [...prev, {
                id: Date.now() + 1,
                type: 'bot',
                content: 'Sorry, I encountered an error processing your request. Please ensure the backend is running.'
            }]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <h1>Splunk Intelligent Layer</h1>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.5)' }}>Powered by</span>
                    <select style={{ background: 'rgba(0,0,0,0.3)', color: 'white', border: 'none', borderRadius: '4px', padding: '4px' }}>
                        <option>OpenAI GPT-4</option>
                        <option>Gemini Pro</option>
                        <option>Claude 3</option>
                    </select>
                </div>
            </div>

            <div className="chat-messages">
                {messages.map((msg) => (
                    <div key={msg.id} className={`message ${msg.type}`}>
                        <div>{msg.content}</div>
                        {msg.spl && (
                            <div className="spl-code">
                                SPL: {msg.spl}
                            </div>
                        )}
                    </div>
                ))}
                {isLoading && (
                    <div className="message bot">
                        <span className="loading-dots">Thinking...</span>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            <div className="example-prompts">
                {EXAMPLE_PROMPTS.map((prompt, i) => (
                    <div key={i} className="example-chip" onClick={() => setInput(prompt)}>
                        {prompt}
                    </div>
                ))}
            </div>

            <div className="input-area">
                <form className="input-wrapper" onSubmit={handleSubmit}>
                    <textarea
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSubmit();
                            }
                        }}
                        placeholder="Ask a question about your logs..."
                        rows={1}
                    />
                    <button type="button" className="send-btn" onClick={handleSubmit} disabled={isLoading}>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                    </button>
                </form>
            </div>
        </div>
    );
}
