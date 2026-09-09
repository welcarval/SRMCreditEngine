export interface Fund {
    id: number;
    nome: string;
    cnpj?: string | null;
    taxaBase?: number | null;
    contaId?: number | null;
    conta?: {
        id?: number;
        identificador?: string | null;
    } | null;
}

export interface Company {
    id: number;
    razaoSocial: string;
}

export interface ReceivableType {
    id: number;
    nome: string;
    spread?: number | null;
}

export interface Receivable {
    id: number;
    valorFace?: number | null;
    valorPresente?: number | null;
    dataVencimento?: string | null;
    fundoId?: number | null;
    tipoId?: number | null;
    empresaId?: number | null;
    prazo?: number | null;
    spread?: number | null;
    taxaBase?: number | null;
    fundo?: Fund | null;
    empresa?: Company | null;
    tipo?: {
        id?: number;
        spread?: number | null;
    } | null;
}

export interface FundPayload {
    id?: number;
    nome: string;
    cnpj: string;
    taxaBase: number;
    contaId: number | null;
}

export interface ReceivablePayload {
    id?: number;
    valorFace: number;
    valorPresente: number;
    dataVencimento: string;
    fundoId: number;
    tipoId: number;
    empresaId: number;
    prazo: number;
    spread: number;
    taxaBase: number;
}
