package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "13_158");
					put("commander2", "13_158");
					// Lord of Moria: [dwarven] Free Peoples support-area condition, not unique ("Each Dwarf is damage +1")
					put("lom1", "1_21");
					put("lom2", "1_21");
					put("lom3", "1_21");
					put("lom4", "1_21");
					// A Path Appointed: [gondor] Free Peoples support-area condition of another title; its abilities are never used
					put("path", "8_42");
					// The Last Alliance of Elves and Men: [elven] condition borne by a [gondor] Man; strength only
					put("alliance", "1_49");
					// Armor: a Free Peoples POSSESSION borne by a Man
					put("armor", "1_92");
					// Strength Born of Fear: a Shadow CONDITION in the support area; its skirmish ability is never used
					put("fear", "1_278");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf, resistance 6
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer, resistance 5
					put("barliman", "1_70");    // Barliman Butterbur: an ally, so no companion; his ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AssaultCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Assault Commander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Shadow:</b> Exert this minion and spot a Free Peoples condition to make an unbound companion
		 * 	resistance -1 for each card that has the same card title as that condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander1");

		assertEquals("Assault Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToMakeAnUnboundCompanionResistanceMinusOneForEachCardOfTheSpottedTitleUntilTheEndOfThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var lom1 = scn.GetFreepsCard("lom1");
		var lom2 = scn.GetFreepsCard("lom2");
		var lom3 = scn.GetFreepsCard("lom3");
		var lom4 = scn.GetFreepsCard("lom4");
		var path = scn.GetFreepsCard("path");
		var alliance = scn.GetFreepsCard("alliance");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(aragorn, alliance, armor);
		scn.MoveCardsToSupportArea(lom1, lom2, path);
		// Two more Lords of Moria that are not in play: one in hand, one in the discard pile.
		scn.MoveCardsToHand(lom3);
		scn.MoveCardsToDiscard(lom4);

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(armor, aragorn);
		assertInZone(Zone.SUPPORT, lom1, lom2, path, fear);
		assertInZone(Zone.HAND, lom3);
		assertInDiscard(lom4);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(commander1));
		assertEquals(0, scn.GetWoundsOn(commander2));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander1));

		scn.ShadowUseCardAction(commander1);
		// The minion whose ability it is exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(commander1));
		assertEquals(0, scn.GetWoundsOn(commander2));
		// Free Peoples conditions of three cultures, in the support area and borne; not a Free Peoples possession,
		// nor a Shadow condition.
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lom1, lom2, path, alliance));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lom1);

		// Unbound companions of two cultures and races; not the Ring-bearer, not Ring-bound Sam, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);
		// Two Lords of Moria in play, the one spotted included; the copies in hand and in the discard pile are
		// not counted, nor is A Path Appointed.
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Read above still in force at the Shadow phase's next decision; the phase ends with the Shadow
		// player's pass, and in the maneuver phase it is gone.
		scn.ShadowPass();
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ABorneConditionWithNoOtherCardOfItsTitleMakesTheCompanionResistanceMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var alliance = scn.GetFreepsCard("alliance");
		var lom1 = scn.GetFreepsCard("lom1");
		var lom2 = scn.GetFreepsCard("lom2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, alliance);
		scn.MoveCardsToSupportArea(lom1, lom2);

		var commander1 = scn.GetShadowCard("commander1");

		scn.StartGame();
		scn.MoveMinionsToTable(commander1);
		assertAttachedTo(alliance, aragorn);
		assertInZone(Zone.SUPPORT, lom1, lom2);
		scn.FreepsPass();

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(commander1);
		scn.ShadowChooseCard(alliance);
		scn.ShadowChooseCard(aragorn);
		// One card of that title: the Alliance itself.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoFreePeoplesConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);

		var commander1 = scn.GetShadowCard("commander1");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1);
		// A Free Peoples possession and a Shadow condition are in play; no Free Peoples condition is.
		assertAttachedTo(armor, aragorn);
		assertInZone(Zone.SUPPORT, fear);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(commander1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander1));
	}

	@Test
	public void ShadowAbilityIsNotOfferedOnceThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(path);

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(commander1, 2);
		assertEquals(2, scn.GetWoundsOn(commander1));
		assertEquals(0, scn.GetWoundsOn(commander2));
		assertInZone(Zone.SUPPORT, path);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander1));
		assertTrue(scn.ShadowActionAvailable(commander2));
	}

	@Test
	public void WithNoUnboundCompanionTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(path);

		var commander1 = scn.GetShadowCard("commander1");

		scn.StartGame();
		scn.MoveMinionsToTable(commander1);
		assertInZone(Zone.SUPPORT, path);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(commander1));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// An ability with a cost is offered whatever its effect can do.
		assertTrue(scn.ShadowActionAvailable(commander1));

		scn.ShadowUseCardAction(commander1);
		// The one Free Peoples condition is spotted unasked, and no companion is unbound.
		assertEquals(1, scn.GetWoundsOn(commander1));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
