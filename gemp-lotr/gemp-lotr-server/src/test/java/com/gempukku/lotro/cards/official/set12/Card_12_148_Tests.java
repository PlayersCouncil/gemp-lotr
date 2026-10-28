package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tempest", "12_148");
					put("uruk", "11_183");      // Feral Uruk: [uruk-hai] minion, strength 9; stronger at a battleground
					// Hounding Uruk: an [uruk-hai] Uruk-hai minion; strength +3 only while a companion is exhausted.
					put("hounding", "11_188");
					// Saruman, Curunir: an [uruk-hai] minion who is a Wizard and no Uruk-hai; a trigger on a hunter's
					// win and a skirmish ability that is never used.
					put("saruman", "17_115");
					// Cirith Ungol Sentry: a [sauron] Uruk-hai minion, whose whole text is Damage +1.
					put("sentry", "10_84");
					// Worthy of Mordor: an [uruk-hai] condition (support area); a trigger at the start of the regroup
					// phase, never reached.
					put("worthy", "12_160");
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("pillage", "4_167");    // Pillage of Rohan: Shadow condition, plays on a site you control; idle with no Uruk-hai stacked there
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");
					put("weary3", "4_212");
					// Black Breath: [wraith] condition; borne, it keeps wounds and burdens on its bearer, and no heal
					// or burden removal is made here
					put("breath", "1_207");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, resistance 6; a maneuver ability that is never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, resistance 6; a skirmish ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, resistance 6; an archery ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, resistance 5
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; stronger against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally; his texts are for the start of a turn and the fellowship phase
					put("promise", "2_112");    // A Promise: Free Peoples condition, support area; needs Sam to do anything
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition, support area; only for a Dwarf against a [moria] minion
					// Watch and Wait: [gandalf] condition on a companion who is not the Ring-bearer; its trigger is on
					// the bearer's win, never reached
					put("watch", "12_35");
					put("tale", "1_66");        // The Tale of Gil-galad: Free Peoples condition, bearer must be an Elf; a skirmish ability
					put("armor", "1_8");        // Dwarven Armor: a possession on a Dwarf; a bar on overwhelming, which no skirmish here reaches
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo 1_290, the Ring-bearer: resistance 10
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TempestofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Tempest of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot an [uruk-hai] minion, each unbound companion is resistance -1 for each condition he or she bears.<br><b>Skirmish:</b> Spot an [uruk-hai] minion and a companion who has resistance 2 or less to discard from play all conditions in all players' support areas.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tempest");

		assertEquals("Tempest of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAnUrukHaiMinionEachUnboundCompanionLosesOneResistanceForEachConditionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("tempest");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var weary3 = scn.GetShadowCard("weary3");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var stairs = scn.GetFreepsCard("stairs");
		var watch = scn.GetFreepsCard("watch");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, gimli, legolas, sam);
		scn.MoveCardsToSupportArea(tempest, worthy, stairs);
		// Two conditions on Aragorn, one of each side; one on Gimli beside a possession; none on Legolas; one each on
		// Sam (Ring-bound) and on the Ring-bearer.
		scn.AttachCardsTo(aragorn, watch, weary1);
		scn.AttachCardsTo(gimli, weary2, armor);
		scn.AttachCardsTo(sam, weary3);
		scn.AttachCardsTo(frodo, breath);
		// A [sauron] Uruk-hai minion, and [uruk-hai] cards that are no minion: nothing is spotted.
		scn.MoveMinionsToTable(sentry);
		assertAttachedTo(watch, aragorn);
		assertAttachedTo(weary1, aragorn);
		assertAttachedTo(weary2, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(weary3, sam);
		assertAttachedTo(breath, frodo);

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// Saruman: an [uruk-hai] minion who is no Uruk-hai.
		scn.MoveMinionsToTable(saruman);

		assertEquals(4, scn.GetResistance(aragorn));     // 6 - 2 conditions
		assertEquals(5, scn.GetResistance(gimli));       // 6 - 1 condition; the armor is no condition
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedOnceACompanionHasResistance2OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("tempest");
		var hounding = scn.GetShadowCard("hounding");
		var worthy = scn.GetShadowCard("worthy");
		var weary1 = scn.GetShadowCard("weary1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var stairs = scn.GetFreepsCard("stairs");
		var watch = scn.GetFreepsCard("watch");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(tempest, worthy, stairs);
		scn.AttachCardsTo(aragorn, watch, weary1);
		scn.MoveMinionsToTable(hounding);
		assertInZone(Zone.SUPPORT, tempest, worthy, stairs);
		assertAttachedTo(watch, aragorn);
		assertAttachedTo(weary1, aragorn);
		scn.AddBurdens(1);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, hounding);
		scn.FreepsPass();

		// Aragorn is 6 - 1 burden - 2 conditions; Elrond, an ally, has resistance 0 and is no companion.
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(legolas));
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(elrond));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tempest));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetResistance(aragorn));
		assertTrue(scn.ShadowActionAvailable(tempest));
		scn.ShadowUseCardAction(tempest);

		assertInDiscard(tempest, worthy, stairs);
		assertAttachedTo(watch, aragorn);
		assertAttachedTo(weary1, aragorn);
		// With the Tempest gone, Aragorn is 6 - 2 burdens.
		assertEquals(4, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithoutAnUrukHaiMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("tempest");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, legolas);
		// A [sauron] Uruk-hai skirmishing, and [uruk-hai] conditions that are no minion.
		scn.MoveCardsToSupportArea(tempest, worthy);
		scn.MoveMinionsToTable(sentry);
		assertInZone(Zone.SUPPORT, tempest, worthy);
		scn.AddBurdens(4);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, sentry);
		scn.FreepsPass();

		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(legolas));
		assertEquals(6, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tempest));
	}

	@Test
	public void SkirmishAbilityIsOfferedWithSarumanWhoIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("tempest");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(tempest);
		scn.MoveMinionsToTable(saruman);
		assertInZone(Zone.SUPPORT, tempest);
		scn.AddBurdens(4);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, saruman);
		scn.FreepsPass();

		assertEquals(2, scn.GetResistance(legolas));
		assertTrue(scn.ShadowActionAvailable(tempest));
		scn.ShadowUseCardAction(tempest);

		assertInDiscard(tempest);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTheConditionsAnAllyAndAControlledSiteBearAndNotThoseACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("tempest");
		var uruk = scn.GetShadowCard("uruk");
		var pinned = scn.GetShadowCard("pinned");
		var pillage = scn.GetShadowCard("pillage");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var promise = scn.GetFreepsCard("promise");
		var tale = scn.GetFreepsCard("tale");
		var watch = scn.GetFreepsCard("watch");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(promise, tempest, pinned);
		// In a support area: the conditions lying in either player's, those an ally bears (of either player's)
		// and the one on a site the Shadow player controls.
		scn.AttachCardsTo(elrond, tale, weary1);
		scn.AttachCardsTo(site1, pillage);
		// In none: the conditions companions bear.
		scn.AttachCardsTo(arwen, watch);
		scn.AttachCardsTo(aragorn, weary2);
		scn.MoveMinionsToTable(uruk);
		assertInZone(Zone.SUPPORT, promise, tempest, pinned);
		assertAttachedTo(tale, elrond);
		assertAttachedTo(weary1, elrond);
		assertAttachedTo(pillage, site1);
		assertAttachedTo(watch, arwen);
		assertAttachedTo(weary2, aragorn);
		// The Ring-bearer's resistance is 10 less the burdens: 8 burdens leave 2
		scn.AddBurdens(8);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, uruk);
		scn.FreepsPass();

		assertEquals(2, scn.GetResistance(frodo));
		assertTrue(scn.ShadowActionAvailable(tempest));
		scn.ShadowUseCardAction(tempest);

		assertInDiscard(promise, tale, weary1, pillage, pinned, tempest);
		assertAttachedTo(watch, arwen);
		assertAttachedTo(weary2, aragorn);
		// The site is no condition and is still the Shadow player's
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
