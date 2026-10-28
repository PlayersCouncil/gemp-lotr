package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "12_28");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus by races is his whole text
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a bonus beside 3 Ents only
					put("savage", "1_151");     // Uruk Savage: "Damage +1." is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains) as site 2: site 1, Eastemnet Gullies 4_325, and
	// site 3, Barrows of Edoras 4_337 (Sanctuary), are no battlegrounds; site 4, White Rocks 4_346, is one.  Uruk
	// Camp's text answers an Uruk-hai played, and none is.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "12_28");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus by races is his whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsHatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Gandalf's Hat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Bearer must be Gandalf.<br>Each time the fellowship moves from a battleground site, add a burden.<br><b>Regroup:</b> Add 2 burdens to remove all twilight tokens from the twilight pool.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hat");

		assertEquals("Gandalf's Hat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hat = scn.GetFreepsCard("hat");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(hat);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Gandalf; nor is the Ring-bearer.
		assertInHand(hat);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hat));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(hat));
		// Gandalf is the one bearer it can go on: it goes on him unasked.
		scn.FreepsPlayCard(hat);
		assertAttachedTo(hat, gandalf);
		assertNotAttachedTo(hat, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveFromABattlegroundAddsABurdenAndAMoveToOneOrFromAnotherSiteDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var hat = scn.GetFreepsCard("hat");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, hat);

		scn.StartGame();
		assertAttachedTo(hat, gandalf);
		assertEquals(0, scn.FreepsGetBurdens());

		// From Eastemnet Gullies to Uruk Camp: a move TO a battleground, from a site that is none.
		scn.FreepsPass();
		assertEquals("Uruk Camp", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.FreepsGetBurdens());

		// From Uruk Camp to Barrows of Edoras, in the regroup phase: a burden.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Barrows of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.FreepsGetBurdens());

		// The next turn, from Barrows of Edoras to White Rocks: no burden.
		scn.SkipToSite(4);
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.FreepsGetBurdens());

		// From White Rocks to site 5: a burden again.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.FreepsGetBurdens());
		assertAttachedTo(hat, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionAddsTwoBurdensToRemoveEveryTwilightToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hat = scn.GetFreepsCard("hat");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, hat);

		scn.StartGame();
		assertAttachedTo(hat, gandalf);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hat));

		scn.FreepsUseCardAction(hat);
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hat = scn.GetFreepsCard("hat");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, hat);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(hat, gandalf);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// A minion on the table, twilight in the pool and burdens that can be added.
		assertTrue(scn.GetTwilight() > 0);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hat));
	}
}
