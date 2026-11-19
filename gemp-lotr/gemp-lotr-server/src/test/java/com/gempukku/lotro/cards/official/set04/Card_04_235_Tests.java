package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_235_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gathering", "4_235");

					// Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Easterling Infantry: [raider] Easterling Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); text only at 3 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man minion with no text
					put("brigand", "4_10");

					// Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Boromir, Steward's Heir: a Ring-bound [gondor] Man companion who is not the Ring-bearer; keywords only
					put("boromir", "0_65");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatheringtotheSummonsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gathering to the Summons
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot a [raider] Man skirmishing a Ring-bound companion to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gathering");

		assertEquals("Gathering to the Summons", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInAnUnboundCompanionsSkirmishAndPlayedInTheRingBearersToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gathering);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{frodo, infantry});

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		// A [raider] Man skirmishing a companion who is not Ring-bound, and another assigned to the Ring-bearer
		// whose skirmish has not begun.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gathering));
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(gathering));
		scn.ShadowPlayCard(gathering);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASouthronSkirmishingARingBoundManWhoIsNotTheRingBearerLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var explorer = scn.GetShadowCard("explorer");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(gathering);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, explorer);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(gathering));
		scn.ShadowPlayCard(gathering);

		// The burden goes on the Ring-bearer, who is in no skirmish.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhenTheRingBearerIsSkirmishedOnlyByARaiderMinionWhoIsNoManAndAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(gathering);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, khamul);
		// The Brigand, left over, goes to Frodo too.
		scn.ShadowAssignToMinions(frodo, brigand);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, khamul));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gathering));
	}
}
