package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_298_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coneys1", "4_298");
					put("coneys2", "4_298");
					// Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer; abilities used only on purpose
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: an unbound Hobbit companion; +2 only while he bears a weapon
					put("merry", "1_303");
					// Bounder: a Hobbit ally (only companions are Ring-bound); a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a Ring-bound [shire] companion who is no Hobbit; an assignment ability
					// used only on purpose
					put("smeagol", "103_116");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BraceofConeysStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Brace of Coneys
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Bearer must be a Ring-bound Hobbit.<br><b>Fellowship:</b> Discard this possession to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coneys1");

		assertEquals("Brace of Coneys", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARingBoundHobbitOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coneys = scn.GetFreepsCard("coneys1");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(coneys);

		scn.StartGame();
		assertInHand(coneys);
		assertTrue(scn.FreepsPlayAvailable(coneys));

		scn.FreepsPlayCard(coneys);
		// The Ring-bearer and Sam; not an unbound Hobbit, a Hobbit ally, nor a Ring-bound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertInZone(Zone.ATTACHED, coneys);
		assertAttachedTo(coneys, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsThatCopyToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coneys1 = scn.GetFreepsCard("coneys1");
		var coneys2 = scn.GetFreepsCard("coneys2");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, coneys1);
		scn.AttachCardsTo(sam, coneys2);

		scn.StartGame();
		scn.AddBurdens(2);
		assertInZone(Zone.ATTACHED, coneys1, coneys2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(coneys2));

		scn.FreepsUseCardAction(coneys2);

		// Sam's copy pays; Frodo's stays.
		assertInDiscard(coneys2);
		assertInZone(Zone.ATTACHED, coneys1);
		assertAttachedTo(coneys1, frodo);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var coneys = scn.GetFreepsCard("coneys1");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, coneys);

		scn.StartGame();
		scn.AddBurdens(1);
		assertInZone(Zone.ATTACHED, coneys);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(coneys));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(coneys));
		assertInZone(Zone.ATTACHED, coneys);
		assertEquals(1, scn.GetBurdens());
	}
}
