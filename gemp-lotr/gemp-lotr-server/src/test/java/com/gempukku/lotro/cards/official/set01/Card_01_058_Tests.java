package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seen", "1_58");

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; her bonus is against a Nazgul only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3; an archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally who is no Elf; a fellowship ability, never used

					// Shoulder to Shoulder: a Free Peoples condition in the support area; a maneuver ability only
					put("shoulder", "1_59");
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession on a Dwarf, no condition
					// Greed: a Shadow condition in the support area; its ability needs 6 companions
					put("greed", "1_125");
					// You Bring Great Evil: a Shadow condition on a companion who is no Elf; matters only at a sanctuary
					put("evil", "1_283");
					// Spider Poison: a Shadow possession borne by a companion, no condition; its trigger answers an assignment
					put("poison", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSeenandtheUnseenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Seen and the Unseen
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert 2 Elves to discard a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seen");

		assertEquals("The Seen and the Unseen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingTwoElvesDiscardsAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seen = scn.GetFreepsCard("seen");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		var shoulder = scn.GetFreepsCard("shoulder");
		var armor = scn.GetFreepsCard("armor");
		var greed = scn.GetShadowCard("greed");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		scn.MoveAlliesToTable(orophin, grimir);
		scn.MoveCardsToSupportArea(shoulder, greed);
		scn.AttachCardsTo(gimli, armor, evil);
		scn.AttachCardsTo(arwen, poison);
		scn.MoveCardsToHand(seen);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInZone(Zone.SUPPORT, shoulder, greed);
		assertAttachedTo(evil, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(poison, arwen);
		assertTrue(scn.FreepsPlayAvailable(seen));

		scn.FreepsPlayCard(seen);
		// Elves of either kind, companion and ally; not a companion or an ally who is no Elf.  Two of them, each once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, orophin);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Any condition, Shadow or Free Peoples, in a support area or borne; not a possession of either side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shoulder, greed, evil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, poison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(evil);

		assertInZone(Zone.DISCARD, evil, seen);
		assertInZone(Zone.SUPPORT, shoulder, greed);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(poison, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableWithOnlyOneElfAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seen = scn.GetFreepsCard("seen");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var greed = scn.GetShadowCard("greed");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin, grimir);
		scn.MoveCardsToSupportArea(greed);
		scn.MoveCardsToHand(seen);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Orophin is exhausted.  Legolas could exert twice over, and Gimli and Grimir,
		// who are no Elves, could exert too.
		scn.AddWoundsToChar(orophin, 2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(seen));

		// The control: the wounds taken off, and two Elves can pay.
		scn.RemoveWoundsFromChar(orophin, 2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(seen));
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTwoElvesExertAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seen = scn.GetFreepsCard("seen");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var armor = scn.GetFreepsCard("armor");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(legolas, gimli, arwen);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(arwen, poison);
		scn.MoveCardsToHand(seen);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(poison, arwen);
		// The condition is the event's target, not a requirement: with only possessions in play it is still played.
		assertTrue(scn.FreepsPlayAvailable(seen));

		scn.FreepsPlayCard(seen);
		scn.FreepsChooseCards(legolas, orophin);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(poison, arwen);
		assertInZone(Zone.DISCARD, seen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
