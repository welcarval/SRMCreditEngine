export interface Fund {
    id: number;
    nome: string;
    cnpj?: string | null;
    taxaBase: number;
    contaId?: number | null;
    saldo?: number | null;
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
    taxaBase: number;
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
    valorPresente?: number | null;
    dataVencimento: string;
    fundoId: number | null;
    tipoId: number;
    empresaId: number;
    prazo?: number | null;
    spread?: number | null;
    taxaBase: number;
}

export interface UserAccess {
    id: number;
    nome: string;
    email: string;
    tipo: string;
    fundoIds: number[];
}
