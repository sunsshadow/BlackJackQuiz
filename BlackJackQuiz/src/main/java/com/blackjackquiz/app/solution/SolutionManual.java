package com.blackjackquiz.app.solution;

import android.content.Context;
import android.database.Cursor;
import com.blackjackquiz.app.deck.Deck.Card;

import java.util.HashMap;
import java.util.Map;

public class SolutionManual
{
    // See makedb/makedb.rb for the MOVES_MAP mapping values
    public enum BlackJackAction
    {
        Hit(0),
        Double(1),
        Stand(2),
        Split(3),
        DoubleAfterSplit(4);

        BlackJackAction(int value)
        {

            this.value = value;
        }

        private final int value;
    }

    private enum HandType
    {
        Hard("hard"),
        Soft("soft"),
        Split("split");

        HandType(String table)
        {
            this.table = table;
        }

        private final String table;
    }

    private static Map<Integer, BlackJackAction> s_codeToActionMap = new HashMap<>();
    static
    {
        for (BlackJackAction action : BlackJackAction.values())
        {
            s_codeToActionMap.put(action.value, action);
        }
    }

    public SolutionManual(Context context)
    {
        m_db = new Database(context);
    }

    public BlackJackAction getSolutionForCards(final Card dealerCard,
                                               final Card playerCardOne,
                                               final Card playerCardTwo)
    {
        final HandType handType = getHandTypeFromCards(playerCardOne, playerCardTwo);
        final int playerCardValue = getValueFromCards(handType, playerCardOne, playerCardTwo);

        return DbUtils.singleItemQuery(m_db, new DbUtils.SingleItemQuerier<BlackJackAction>()
        {
            @Override
            public Cursor performQuery(Database.Transaction transaction)
            {
                return transaction.query("SELECT action FROM " + handType.table + " WHERE dealer_card=? AND player_card_value=?",
                                         new String[]{String.valueOf(dealerCard.rank.value),
                                                      String.valueOf(playerCardValue)});
            }

            @Override
            public BlackJackAction process(Cursor cursor)
            {
                return s_codeToActionMap.get(cursor.getInt(0));
            }
        });
    }

    private static HandType getHandTypeFromCards(Card cardOne, Card cardTwo)
    {
        if (cardOne.equals(cardTwo))
        {
            return HandType.Split;
        }

        if (cardOne.isAce() || cardTwo.isAce())
        {
            return HandType.Soft;
        }
        else
        {
            return HandType.Hard;
        }
    }

    private static int getValueFromCards(HandType handType, Card cardOne, Card cardTwo)
    {
        if (handType == HandType.Split)
        {
            return cardOne.rank.value;
        }
        else
        {
            return cardOne.rank.value + cardTwo.rank.value;
        }
    }

    private final Database m_db;
}