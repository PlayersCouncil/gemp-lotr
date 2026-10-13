package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_013_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("taurnil", "55_13");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; Ranger, +3 only against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas
					put("bow", "1_41");         // Elven Bow: ranged weapon, cost 1, bearer must be an Elf, bearer is an archer
					put("bow2", "4_62");        // Elven Bow: the same text, a different card
					put("longbow", "6_21");     // Naith Longbow: ranged weapon, cost 1, an archery ability, no archer keyword
					put("sword", "4_64");       // Elven Sword: hand weapon, bearer must be an Elf, a skirmish ability

					put("axe", "4_26");         // Iron Axe: [dunland] Shadow possession, cost 1, bearer must be a [dunland] Man
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] Shadow possession, cost 0
					put("threatening", "4_6");  // Constantly Threatening: [dunland] Shadow condition, cost 1
					put("palantir", "3_67");    // The Palantir of Orthanc: [isengard] Shadow artifact, cost 0
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TaurnilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Taurnil, Sharp-eyed Bowman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * To play, spot an Elf.
		 * While Taurnil bears a ranged weapon, the twilight cost of each Shadow possession is +2, and he does not
		 * add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("taurnil");

		assertEquals("Taurnil", card.getBlueprint().getTitle());
		assertEquals("Sharp-eyed Bowman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void TaurnilCanBePlayedForTwoOnlyOnceAnElfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taurnil = scn.GetFreepsCard("taurnil");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(taurnil, arwen);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertFalse(scn.FreepsPlayAvailable(taurnil));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(taurnil));

		scn.FreepsPlayCard(taurnil);
		// 2 for Arwen + 2 for Taurnil
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, taurnil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlayTaurnil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taurnil = scn.GetFreepsCard("taurnil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(taurnil);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(taurnil));
		scn.FreepsPlayCard(taurnil);
		assertInZone(Zone.FREE_CHARACTERS, taurnil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowPossessionsCostTwoMoreOnceTaurnilIsGivenARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taurnil = scn.GetFreepsCard("taurnil");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var sword = scn.GetFreepsCard("sword");
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(taurnil, arwen);
		// A hand weapon on Taurnil, and a ranged weapon on another Elf.
		scn.AttachCardsTo(taurnil, sword);
		scn.AttachCardsTo(arwen, bow2);
		scn.MoveCardsToHand(bow, longbow);

		var axe = scn.GetShadowCard("axe");
		var scimitar = scn.GetShadowCard("scimitar");
		var threatening = scn.GetShadowCard("threatening");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToHand(axe, scimitar, threatening, palantir);

		scn.StartGame();

		assertEquals(1, scn.GetTwilightCost(axe));
		assertEquals(0, scn.GetTwilightCost(scimitar));
		assertEquals(1, scn.GetTwilightCost(threatening));
		assertEquals(0, scn.GetTwilightCost(palantir));
		assertEquals(1, scn.GetTwilightCost(longbow));

		// Arwen already bears a ranged weapon, so Taurnil is the one bearer the bow can go to.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, taurnil);

		assertEquals(3, scn.GetTwilightCost(axe));
		assertEquals(2, scn.GetTwilightCost(scimitar));
		assertEquals(1, scn.GetTwilightCost(threatening));
		assertEquals(0, scn.GetTwilightCost(palantir));
		assertEquals(1, scn.GetTwilightCost(longbow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileTaurnilBearsALongbowTheShadowPlayerPaysTwoMoreToPlayAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taurnil = scn.GetFreepsCard("taurnil");
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(taurnil);
		scn.AttachCardsTo(taurnil, longbow);

		var axe = scn.GetShadowCard("axe");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// 1 printed + 2
		assertEquals(pool - 3, scn.GetTwilight());
		assertAttachedTo(axe, brigand);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TaurnilStopsAddingToTheArcheryTotalWhenHeIsGivenARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taurnil = scn.GetFreepsCard("taurnil");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(taurnil, arwen);
		// A hand weapon on Taurnil, and a ranged weapon on another Elf.
		scn.AttachCardsTo(taurnil, sword);
		scn.AttachCardsTo(arwen, bow2);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		// An archer by other means than a ranged weapon.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, taurnil, null, Keyword.ARCHER));

		// Arwen and Taurnil: his hand weapon and her ranged weapon change nothing about him.
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Arwen already bears a ranged weapon, so Taurnil is the one bearer the bow can go to.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, taurnil);

		// Arwen alone, though Taurnil is still an archer.
		assertTrue(scn.HasKeyword(taurnil, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
