package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_308_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knocked", "4_308");
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] unbound Hobbit; a skirmish ability used only on purpose
					// Pippin, Trollslayer: a [gondor] unbound Hobbit; to play, spot a [gondor] companion (Boromir)
					put("trollslayer", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					put("boromir", "0_65");     // Boromir, Steward's Heir: a [gondor] companion with keywords only
					// Pippin, Friend to Frodo: an unbound Hobbit, laid in the OTHER player's discard pile
					put("pippin", "1_306");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnockedontheHeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Knocked on the Head
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Play an unbound Hobbit from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knocked");

		assertEquals("Knocked on the Head", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAnUnboundHobbitOfAnyCultureFromYourDiscardPileAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knocked = scn.GetFreepsCard("knocked");
		var merry = scn.GetFreepsCard("merry");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetShadowCard("pippin");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToDiscard(merry, trollslayer, sam, maggot, aragorn, pippin);
		scn.MoveCardsToHand(knocked);

		scn.StartGame();
		assertInDiscard(merry, trollslayer, sam, maggot, aragorn, pippin);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(knocked));
		scn.FreepsPlayCard(knocked);

		// A [shire] and a [gondor] unbound Hobbit; not a Ring-bound Hobbit, a Hobbit ally, an unbound Man,
		// nor the other player's unbound Hobbit in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, trollslayer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, maggot, aragorn, pippin));
		scn.FreepsChooseCard(trollslayer);

		assertInZone(Zone.FREE_CHARACTERS, trollslayer);
		assertInDiscard(knocked, merry, sam, maggot, aragorn, pippin);
		// The regroup phase goes on, the Shadow player next to act.
		assertTrue(scn.ShadowDecisionAvailable("Play Regroup action or Pass"));
	}

	@Test
	public void CannotBePlayedWithNoUnboundHobbitInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knocked = scn.GetFreepsCard("knocked");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetShadowCard("pippin");
		scn.MoveCardsToDiscard(sam, maggot, aragorn, pippin);
		scn.MoveCardsToHand(knocked);

		scn.StartGame();
		assertInDiscard(sam, maggot, aragorn, pippin);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(knocked));
		assertInHand(knocked);
	}
}
