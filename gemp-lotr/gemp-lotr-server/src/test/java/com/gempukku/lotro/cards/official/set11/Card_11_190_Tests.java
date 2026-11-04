package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_190_Tests
{

	// Site 2 is Eastfold 4_331 (plains, no battleground); site 4 is White Rocks 4_346 (battleground only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "11_190");

					put("watchman", "11_206");  // Watchman Uruk: an [uruk-hai] Uruk-hai, vitality 2; damage +1 and a trigger on its own play, not reached by a cheat
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard (no Uruk-hai), vitality 4; each companion resistance -1, and a skirmish ability
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1; its play trigger is not reached by a cheat

					put("baruk", "5_5");        // Baruk Khazad: Maneuver event, exert a Dwarf to wound a minion
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf who exerts for it; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a bar on discarding [shire] tales only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, no companion; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Uruk Camp 4_335 (battleground and plains; its trigger answers an Uruk-hai played, and no minion is played here).
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "11_190");

					put("watchman", "11_206");  // Watchman Uruk: an [uruk-hai] Uruk-hai, vitality 2; damage +1 and a trigger on its own play, not reached by a cheat
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only

					put("baruk", "5_5");        // Baruk Khazad: Maneuver event, exert a Dwarf to wound a minion
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf who exerts for it; a skirmish ability only
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
	public void InvincibleUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Invincible Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is at a battleground site, it cannot take wounds.<br>While you can spot 6 companions, each [uruk-hai] minion cannot take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk");

		assertEquals("Invincible Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtABattlegroundThisMinionCannotBeWoundedButAnotherUrukHaiCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		var uruk = scn.GetShadowCard("uruk");
		var watchman = scn.GetShadowCard("watchman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(uruk, watchman, guard);
		scn.SkipToPhase(Phase.MANEUVER);

		// White Rocks, site 4: a battleground.  Two companions, so the second sentence is not in force.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(watchman));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchman, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(uruk));
		scn.FreepsChooseCard(watchman);

		assertEquals(1, scn.GetWoundsOn(watchman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtABattlegroundThatIsAlsoAPlainsThisMinionCannotBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		var uruk = scn.GetShadowCard("uruk");
		var watchman = scn.GetShadowCard("watchman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, watchman, guard);
		scn.SkipToPhase(Phase.MANEUVER);

		// Uruk Camp, site 2: a battleground and a plains.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(watchman, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(uruk));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AwayFromABattlegroundWithFiveCompanionsAndAnAllyEveryMinionCanBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var baruk = scn.GetFreepsCard("baruk");
		// Frodo and four more: five companions, and an ally who is no companion.
		scn.MoveCompanionsToTable(gimli, aragorn, boromir, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		var uruk = scn.GetShadowCard("uruk");
		var watchman = scn.GetShadowCard("watchman");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, watchman, saruman, guard);
		scn.SkipToPhase(Phase.MANEUVER);

		// Eastfold, site 2: a plains, and no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(uruk));

		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, watchman, saruman, guard));
		scn.FreepsChooseCard(uruk);

		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixCompanionsNoUrukHaiCultureMinionCanBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var baruk = scn.GetFreepsCard("baruk");
		// Frodo and five more: six companions.
		scn.MoveCompanionsToTable(gimli, aragorn, boromir, merry, pippin);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		var uruk = scn.GetShadowCard("uruk");
		var watchman = scn.GetShadowCard("watchman");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, watchman, saruman, guard, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(baruk);
		// Neither [uruk-hai] Uruk-hai nor the [uruk-hai] Wizard; a [sauron] Uruk-hai and a [moria] Orc may be.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(uruk, watchman, saruman));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(watchman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
