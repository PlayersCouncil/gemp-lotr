package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_019_Tests
{

	// Rivers at sites 2 and 3: Buckleberry Ferry and Ford of Bruinen, which differ in Ford of Bruinen's sanctuary.
	// Site 4, Balin's Tomb, is no river, and is reached from one.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flood", "2_19");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, who is no Elf; a maneuver ability used only on purpose

					put("nelya", "2_84");       // Ulaire Nelya: [wraith] Nazgul, vitality 3; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, vitality 3; fierce; his bonuses need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgul, vitality 2; its bonus needs 2 wounds on the Ring-bearer
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_330");      // Buckleberry Ferry: river; no assignment or skirmish phases while only Hobbits are in the fellowship
					put("site3", "1_338");      // Ford of Bruinen: river, sanctuary; the first Nazgul played there each turn is -5
					put("site4", "1_343");      // Balin's Tomb: underground; a maneuver ability that discards a tale
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReleasetheAngryFloodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Release the Angry Flood
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Elf to wound each Nazgûl once (or twice if at a river).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flood");

		assertEquals("Release the Angry Flood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToWoundEachNazgulOnceAwayFromARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		// The fellowship crosses both rivers and comes to Balin's Tomb, which is none.
		scn.SkipToSite(3);
		scn.MoveCardsToHand(flood);
		scn.MoveMinionsToTable(nelya, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.FreepsPlayAvailable(flood));

		scn.FreepsPlayCard(flood);
		// An Elf companion and an Elf ally may pay; the Man may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Each Nazgul once, whatever his culture; the [wraith] Orc not at all.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(flood);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedNazgulIsStillWoundedAndSoKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// The fellowship crosses both rivers and comes to Balin's Tomb, which is none.
		scn.SkipToSite(3);
		scn.MoveCardsToHand(flood);
		scn.MoveMinionsToTable(nelya, khamul);
		// Vitality 3 with 2 wounds: Ulaire Nelya is exhausted, and a wound is not an exertion.
		scn.AddWoundsToChar(nelya, 2);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInPlay(nelya, khamul);
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsPlayCard(flood);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(khamul));
		// 2 wounds + 1: the third kills her.
		assertInDiscard(nelya);
		assertInDiscard(flood);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(flood);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Legolas cannot exert. Aragorn could, but he is no Elf.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(flood);
		assertFalse(scn.FreepsPlayAvailable(flood));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsStillPlayedAndTheElfExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(flood);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(hound));
		// The text has no requirement: with nothing to wound the event is played and its cost paid.
		assertTrue(scn.FreepsPlayAvailable(flood));
		scn.FreepsPlayCard(flood);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(flood);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtARiverEachNazgulIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(flood);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);

		// Buckleberry Ferry, a river.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));

		// Legolas is the only Elf, so he pays unasked; nor is the player asked whether to wound twice.
		scn.FreepsPlayCard(flood);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Vitality 3: two wounds, not one and not three.
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertInPlay(nelya, khamul);
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(flood);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtARiverThatIsAlsoASanctuaryEachNazgulIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flood = scn.GetFreepsCard("flood");
		var legolas = scn.GetFreepsCard("legolas");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(flood);
		scn.MoveMinionsToTable(nelya, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);

		// Ford of Bruinen, a river and a sanctuary.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));

		scn.FreepsPlayCard(flood);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(flood);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
