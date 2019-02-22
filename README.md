# Scheduler
Scheduler of process

Máteria: Sistemas Operacionais.

USP EACH - Sitemas de informação - 4°Semestre.

Implementação de um escalonador de tarefas para Time Sharing em uma máquina comum único processador.

**Especificação do processador**:

O processador possui apenas 4 instruções:

1. Atribuicão: na forma X=<valor> ou Y=<valor>, onde <valor> é um número inteiro e
X e Y são os registradores de uso geral usados pelo processo.
2. Entrada e saída: representada pela instrucão E/S (que faz as vezes de uma chamada ao
sistema)
3. Comando: a tarefa executada pela máquina, representada pela instrucão COM
4. Fim de programa: chamada com a única finalidade de remover o programa da memória,
executando a limpeza final. Representada pela instrucão SAIDA

**Funcionamento do sistema**:

  O sistema inicia lendo os arquivos txt do diretório processos, pegando o n_com do
arquivo quantum.txt, armazenando em arrayList as prioridades do arquivo prioridades.txt e
colocando os processos na ordem correta na fila de prontos e posicionando o bcp na tabela
de processos (na fila de prontos é armazenado a referência ao bcp na tabela de processos,
representado por um inteiro que é uma chave do HashMap da tabela de processos), além
disso o processo é salvo na memória e o processo guarda a referência(um inteiro) para a
string na memória.

  Assim que os processos finalizam de serem carregados em memória, o So começa
a percorrer a fila de prontos, iterando enquanto a tabela de processos não estiver vazia.O
pega o primeiro da fila de prontos, da fila de créditos apropriada o primeiro elemento da fila,
através da referência ao bcp, o so pega o bcp na tabela de processos e chama
imediatamente o despachador para recuperar o contexto através do bcp, assim que o
despachador dá um retorno para o So, se inicia a rotina de execução, colocando o psw do
bcp como “EXECUTANDO”, iterando enquanto o quantum do processo não estiver
terminado, a cada iteração é incrementado o pc do processador, assim garantindo pegar a
próxima linha do processo (Strings do processo retornado ao recuperar o contexto na
memória), assim que o quantum do processo termina ou ocorre entrada e saída ou termina
o processo, incremente as variáveis usadas nas estatísticas do sistema, verifica qual foi o
motivo de ter saído da cpu, se for por término do processo, chama o escalonador,
escalonador remove da fila de prontos e devolve pro So a referência para o próximo bcp.
Caso tenha entrada e saída o So chama o escalonador, o escalonador bloqueia o processo
e põe no final da fila de bloqueados, inicia contagem de tempo de espera para o processo e
remove da fila de prontos, decrementando seus créditos e dobrando o quantum que vai ter
na próxima iteração. Caso apenas seu quantum tenha terminado, decrementa seus
créditos, chama o escalonador, escalonador reposiciona o processo na fila de créditos
correspondente e dobra o quantum do processo.

  Além disso ao chamar o escalonador, automaticamente o escalonador verifica se
nenhum processo na fila de bloqueados pode retornar para a fila de prontos, se sim
reposiciona na fila de prontos de acordo com seus créditos e remove da fila de bloqueados,
e o escalonador também verifica se todos os processos das duas filas possuem 0 de crédito
, se sim redistribui os seus créditos para continuar executando.Uma observação é que ao
final quando todos os processos na fila de prontos estão com créditos 0 porém na fila de
bloqueados ainda possui processos com algum crédito, é realizado Round-Robin na fila.

  Ao final da rotina de percorrer todas as filas, é calculado as médias relacionadas ao
número de instruções por quantum e número de trocas por processo, e ao final é gerado o
logfile com o número correspondente do quantum do arquivo txt.
