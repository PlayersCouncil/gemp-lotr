package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_003_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "101_3");

					// Three possessions that Gimli may bear together and that add nothing to strength or damage.
					// Dwarven Armor: "Bearer may not be overwhelmed unless his or her strength is tripled."
					put("armor", "1_8");
					// Gimli's Helm: "He takes no more than 1 wound during each skirmish phase", and a skirmish
					// ability.
					put("helm", "1_15");
					// Gimli's Pipe: a fellowship ability, never used.
					put("pipe", "3_2");
					put("axe", "2_10");         // Hand Axe: possession, strength +1; its archery ability is never used
					// Ring of Retribution: artifact, strength +1 and vitality +1.  Its response is to bearer
					// winning a skirmish.
					put("ring", "9_9");
					// Endurance of Dwarves: a condition a Dwarf bears, vitality +1.  Its skirmish ability is
					// never used.
					put("endurance", "2_4");

					put("guard", "1_7");        // Dwarf Guard: another Dwarf, to bear items that are not Gimli's
					put("guardArmor", "1_8");
					put("guardAxe", "2_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gimli, Vengeful Longbeard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Damage +1.
		 * While Gimli bears 2 items, he is damage +1.
		 * While Gimli bears 3 items, he is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Vengeful Longbeard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		assertInHand(gimli);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gimli));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SecondItemMakesGimliDamagePlusOneAndThirdItemStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		var pipe = scn.GetFreepsCard("pipe");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(armor, helm, pipe, axe);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Gimli is the only Dwarf, so each item goes onto him without asking.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// 2 items: his own damage +1 and one more.
		scn.FreepsPlayCard(helm);
		assertAttachedTo(helm, gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// 3 items: strength 6 + 2, and bearing 3 he still bears 2.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, gimli);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// 4 items: nothing more than the Hand Axe's own +1.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The bonuses last only while he bears the items.  Back to 2:
		scn.MoveCardsToDiscard(axe, pipe);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// and back to 1.
		scn.MoveCardsToDiscard(helm);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void OnlyPossessionsAndArtifactsThatGimliBearsCountAsHisItems() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var pipe = scn.GetFreepsCard("pipe");
		var ring = scn.GetFreepsCard("ring");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCompanionsToTable(gimli, guard);
		// Two items in play that are not on Gimli.
		scn.AttachCardsTo(guard, scn.GetFreepsCard("guardArmor"), scn.GetFreepsCard("guardAxe"));

		scn.StartGame();

		// A possession and a condition: 2 cards on him, 1 item.
		scn.AttachCardsTo(gimli, armor, endurance);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// An artifact is an item: 3 cards on him, 2 items.  Strength is 6 + 1 for the ring itself.
		scn.AttachCardsTo(gimli, ring);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// A second possession makes 3 items: 6 + 1 for the ring + 2.
		scn.AttachCardsTo(gimli, pipe);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
