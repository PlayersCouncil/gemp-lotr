package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "11_135");
					put("troll2", "11_135");

					// Denizen of Khazad-dum: an [orc] Orc.  Strength +2 only at an underground site, never reached.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, cost 3, site 4.  Its trigger at the start
					// of the assignment phase is never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");
					// Beastly Olog-hai: another [orc] Troll, cost 6, site 5; to play, spot an [orc] minion.  Its
					// assignment ability is never used.
					put("olog", "11_108");

					// In the Free Peoples hand.  What Are They?: a [gondor] Maneuver event.
					put("whatarethey", "1_119");
					// Stout and Sturdy: a [shire] Maneuver or Skirmish event.
					put("stout", "1_315");
					// Gimli, Son of Gloin: a Free Peoples card that is no event (a companion).
					put("gimli", "1_13");
					// Massing Strength: an event that is no Free Peoples card (an [orc] Skirmish event).
					put("massing", "13_111");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PorterTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Porter Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot an [orc] minion.<br>When you play this minion, add a burden unless the Free Peoples player reveals a Free Peoples event from his or her hand.
		*/

		var scn = GetScenario();

		var troll = scn.GetFreepsCard("troll");

		assertEquals("Porter Troll", troll.getBlueprint().getTitle());
		assertNull(troll.getBlueprint().getSubtitle());
		assertFalse(troll.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troll.getBlueprint().getSide());
		assertEquals(Culture.ORC, troll.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troll.getBlueprint().getCardType());
		assertEquals(Race.TROLL, troll.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertEquals(7, troll.getBlueprint().getTwilightCost());
		assertEquals(15, troll.getBlueprint().getStrength());
		assertEquals(3, troll.getBlueprint().getVitality());
		assertEquals(5, troll.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.StartGame();
		scn.SetShadowHand(troll, halftroll);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [moria] Orc minion and an [orc] condition: no [orc] minion to spot.
		assertFalse(scn.ShadowPlayAvailable(troll));

		// The Half-troll, an [orc] minion who is no Orc, played from hand.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesEventInHandPlayingItAddsABurdenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var massing = scn.GetFreepsCard("massing");
		var troll = scn.GetShadowCard("troll");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		// A Free Peoples companion and a Shadow event: nothing he can reveal.
		scn.SetFreepsHand(gimli, massing);
		scn.SetShadowHand(troll);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetBurdens());
		// An [orc] Orc to spot.
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertFalse(scn.FreepsDecisionAvailable("reveal"));
		assertEquals(1, scn.GetBurdens());
		assertInHand(gimli, massing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayRevealAFreePeoplesEventToPreventTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var stout = scn.GetFreepsCard("stout");
		var gimli = scn.GetFreepsCard("gimli");
		var massing = scn.GetFreepsCard("massing");
		var troll = scn.GetShadowCard("troll");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SetFreepsHand(whatarethey, stout, gimli, massing);
		scn.SetShadowHand(troll);
		scn.MoveMinionsToTable(denizen);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(troll);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reveal"));
		scn.FreepsChooseYes();

		// Free Peoples events of two cultures; not a Free Peoples companion, not a Shadow event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(whatarethey, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, massing));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stout);

		// Shown to the Shadow player; the hand's owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(stout, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.GetBurdens());
		assertInHand(whatarethey, stout, gimli, massing);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedToRevealTheBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var stout = scn.GetFreepsCard("stout");
		var troll = scn.GetShadowCard("troll");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.SetFreepsHand(whatarethey, stout);
		scn.SetShadowHand(troll);
		// An [orc] minion who is no Orc to spot.
		scn.MoveMinionsToTable(halftroll);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(troll);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reveal"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherTrollPlayedAfterItAddsNoBurdenAndASecondCopyAddsOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var olog = scn.GetShadowCard("olog");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SetFreepsHand(gimli);
		scn.SetShadowHand(troll, olog, troll2);
		scn.MoveMinionsToTable(denizen);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(troll);
		assertEquals(1, scn.GetBurdens());

		// Another [orc] Troll played while the Porter Troll is in play: no burden for it.
		scn.ShadowPlayCard(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertFalse(scn.FreepsDecisionAvailable("reveal"));
		assertEquals(1, scn.GetBurdens());

		// A second Porter Troll: one burden more, its own; the first answers its own play only.
		scn.ShadowPlayCard(troll2);
		assertInZone(Zone.SHADOW_CHARACTERS, troll2);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
