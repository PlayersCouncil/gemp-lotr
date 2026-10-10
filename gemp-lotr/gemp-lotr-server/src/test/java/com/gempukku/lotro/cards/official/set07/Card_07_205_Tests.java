package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_205_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_205");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCorruptionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "7_205");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl; her text is about [wraith] conditions
					// Three companions in play, the Ring-bearer among them, hold 3 threats.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("boromir", "1_96");     // Boromir, Lord of Gondor
					// Three characters for the dead pile: two companions and an ally.
					put("gimli", "1_13");       // Gimli, Son of Glóin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("elrond", "1_40");      // Elrond, Lord of Rivendell
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PutForthHisStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Put Forth His Strength
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.<br><b>Shadow:</b> If you have initiative and there are 3 characters in the dead pile, spot 3 burdens, 3 threats and a Nazgûl to corrupt the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Put Forth His Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithInitiativeThreeDeadCharactersThreeBurdensThreeThreatsAndANazgulTheRingBearerIsCorrupted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCorruptionScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("gimli"), scn.GetFreepsCard("legolas"), scn.GetFreepsCard("elrond"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		// Exactly 3 of each, and a Free Peoples hand of fewer than 4 cards, so the Shadow player has initiative.
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.ShadowActionAvailable(strength));

		scn.MarkGameLog();
		scn.ShadowUseCardAction(strength);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.ShadowHasWon());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
	}
}
