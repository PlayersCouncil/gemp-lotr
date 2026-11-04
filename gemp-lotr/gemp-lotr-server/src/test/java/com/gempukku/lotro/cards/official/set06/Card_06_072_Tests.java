package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traitor", "6_72");
					put("traitor2", "6_72");
					put("savage", "1_151");     // Uruk Savage: another [isengard] minion, strength 5, Damage +1 and no other text
					// [isengard] conditions a companion can bear. Weary: "Plays on a companion or ally. Limit 1 per
					// character", whose trigger wants an [isengard] tracker, and none is laid. Banished: plays on a Free
					// Peoples Man; its trigger answers a lost skirmish, and none is fought.
					put("weary", "4_212");
					put("banished", "4_139");
					// Desperate Defense of the Ring: a [sauron] condition on a companion; its trigger answers an
					// assignment, and none is made
					put("defense", "1_244");
					// Gnawing, Biting, Hacking, Burning: an [isengard] condition in the support area, borne by nobody;
					// its stack trigger and Shadow ability are not set off
					put("gnawing", "6_63");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability only
					// Barliman Butterbur: a [gandalf] Man ally; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimTraitorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Rohirrim Traitor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: While you can spot a companion bearing an [isengard] condition, this minion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("traitor");

		assertEquals("Rohirrim Traitor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyIsStrengthPlusTwoOnceAnIsengardConditionIsPlayedOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var traitor = scn.GetShadowCard("traitor");
		var traitor2 = scn.GetShadowCard("traitor2");
		var savage = scn.GetShadowCard("savage");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToHand(weary);

		scn.StartGame();
		scn.MoveMinionsToTable(traitor, traitor2, savage);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(traitor));
		assertEquals(6, scn.GetStrength(traitor2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Frodo is the only character Weary can go on: it goes on him unasked.
		scn.ShadowPlayCard(weary);

		assertAttachedTo(weary, frodo);
		// Each copy is +2 of its own; the other minion is untouched.
		assertEquals(8, scn.GetStrength(traitor));
		assertEquals(8, scn.GetStrength(traitor2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyBearingOneACompanionBearingAConditionOfAnotherCultureOrOneBorneByNobodyDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		var traitor = scn.GetShadowCard("traitor");
		var weary = scn.GetShadowCard("weary");
		var defense = scn.GetShadowCard("defense");
		var gnawing = scn.GetShadowCard("gnawing");
		var banished = scn.GetShadowCard("banished");
		scn.AttachCardsTo(barliman, weary);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToSupportArea(gnawing);

		scn.StartGame();
		scn.MoveMinionsToTable(traitor);

		// An [isengard] condition on an ally, a [sauron] condition on a companion, an [isengard] condition borne by
		// nobody.
		assertAttachedTo(weary, barliman);
		assertAttachedTo(defense, aragorn);
		assertInZone(Zone.SUPPORT, gnawing);
		assertEquals(6, scn.GetStrength(traitor));

		// An [isengard] condition of another title, on a companion of another culture and race.
		scn.AttachCardsTo(aragorn, banished);
		assertAttachedTo(banished, aragorn);
		assertEquals(8, scn.GetStrength(traitor));
	}
}
