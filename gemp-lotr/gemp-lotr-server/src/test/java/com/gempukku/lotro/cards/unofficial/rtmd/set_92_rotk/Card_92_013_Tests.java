package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_013_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hides: [dunland] possession, support area, twilight 1; when played, may draw a card
					put("hides", "4_19");
					put("hides2", "4_19");
					// Goblin Scimitar: [moria] possession (hand weapon), twilight 0, bearer must be a [moria] Orc;
					// when played, may draw a card
					put("scimitar", "1_180");
					// Goblin Patrol Troop: [moria] Orc minion, no text, vitality 3: the Scimitar's bearer, and a minion able to exert
					put("troop", "1_177");
					// Throne of Isengard: a Shadow artifact, support area, twilight 2, nothing needed to play it
					put("throne", "17_39");
					// Saruman's Ambition: a Shadow condition, support area, twilight 0
					put("ambition", "1_133");
					// Old Toby: a Free Peoples possession, support area, twilight 1
					put("toby", "1_305");
					// Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: [dwarven] companion, vitality 3
					put("gimli", "1_13");
					// Bounder: [shire] Hobbit ally, vitality 2: a character who can exert and is no companion
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_13", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hides: [dunland] possession, support area, twilight 1; when played, may draw a card
					put("hides", "4_19");
					put("hides2", "4_19");
					// Goblin Scimitar: [moria] possession (hand weapon), twilight 0, bearer must be a [moria] Orc;
					// when played, may draw a card
					put("scimitar", "1_180");
					// Goblin Patrol Troop: [moria] Orc minion, no text, vitality 3: the Scimitar's bearer, and a minion able to exert
					put("troop", "1_177");
					// Throne of Isengard: a Shadow artifact, support area, twilight 2, nothing needed to play it
					put("throne", "17_39");
					// Saruman's Ambition: a Shadow condition, support area, twilight 0
					put("ambition", "1_133");
					// Old Toby: a Free Peoples possession, support area, twilight 1
					put("toby", "1_305");
					// Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: [dwarven] companion, vitality 3
					put("gimli", "1_13");
					// Bounder: [shire] Hobbit ally, vitality 2: a character who can exert and is no companion
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_13"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_13
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Shadow: Play a possession to make the Free Peoples player exert a companion.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_13", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowPlaysAPossessionToExertCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var hides = scn.GetShadowCard("hides");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(hides);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		int pool = scn.GetTwilight();
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(mod);

		// Shadow plays hides as cost: the only possession in hand, so no choice is asked.  Hides then offers its
		// own "when you play this" draw, which is declined.
		assertInZone(Zone.SUPPORT, hides);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(pool - 1, scn.GetTwilight()); // Hides costs 1
		scn.ShadowDeclineOptionalTrigger();

		// FP player chooses which companion to exert
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EitherPossessionInHandIsPlayedAndTheFreePeoplesPlayerExertsACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var hides = scn.GetShadowCard("hides");
		var discardedHides = scn.GetShadowCard("hides2");
		var scimitar = scn.GetShadowCard("scimitar");
		var troop = scn.GetShadowCard("troop");
		var throne = scn.GetShadowCard("throne");
		var ambition = scn.GetShadowCard("ambition");
		var toby = scn.GetShadowCard("toby");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCardsToHand(hides, scimitar, throne, ambition, toby);
		scn.MoveCardsToDiscard(discardedHides);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(mod));
		assertEquals(5, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(mod);

		// The two Shadow possessions in hand, one for the support area and one a minion bears. Not the artifact
		// or the condition, each of which could be played; not the Shadow player's own Free Peoples possession;
		// not the possession in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hides, scimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(throne, ambition, toby, discardedHides));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scimitar);

		assertAttachedTo(scimitar, troop);
		assertEquals(4, scn.GetShadowHandCount());
		scn.ShadowDeclineOptionalTrigger(); // the Scimitar's own draw

		// The choice is the Free Peoples player's: the Ring-bearer or either other companion, and neither the ally
		// nor the Shadow player's minion (vitality 3, so one that could exert).
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WorksWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// Not owner-gated, so shadow should be able to use it even when freeps owns the meta-site.
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var hides = scn.GetShadowCard("hides");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(hides);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(mod);

		assertInZone(Zone.SUPPORT, hides);
		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotAvailableWithoutPossessionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		// No possession in hand, action shouldn't be available
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotAvailableWhileThePossessionInHandHasNoBearerToBePlayedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var scimitar = scn.GetShadowCard("scimitar");
		var troop = scn.GetShadowCard("troop");
		var throne = scn.GetShadowCard("throne");
		var ambition = scn.GetShadowCard("ambition");
		var toby = scn.GetShadowCard("toby");
		var aragorn = scn.GetFreepsCard("aragorn");

		// The only Shadow possession in hand needs a [moria] Orc to bear it. The artifact and the condition
		// beside it can be played, and so could Old Toby be if the Shadow player might play it.
		scn.MoveCardsToHand(scimitar, throne, ambition, toby);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(throne));
		assertTrue(scn.ShadowPlayAvailable(ambition));
		assertFalse(scn.ShadowPlayAvailable(scimitar));
		assertFalse(scn.ShadowActionAvailable(mod));

		// With an Orc to bear it the Scimitar can be played, and so the cost can be paid.
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(scimitar));
		assertTrue(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
