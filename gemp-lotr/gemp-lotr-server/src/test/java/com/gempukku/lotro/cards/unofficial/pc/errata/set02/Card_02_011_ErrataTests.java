package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_011_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burdens", "52_11");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2, no text but its own play requirement
					put("guard2", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Dwarf

					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, no number on it
					put("armor2", "1_8");
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, only a skirmish-loss trigger
					put("ring", "9_9");         // Ring of Retribution: [dwarven] artifact, strength +1 vitality +1, a response on winning a skirmish
					put("endurance", "2_4");    // Endurance of Dwarves: Free Peoples condition borne by a Dwarf, not an item
					put("stairs", "1_24");      // Stairs of Khazad-dum: Free Peoples condition in the support area

					put("greed", "1_125");      // Greed: Shadow support-area condition, not a weather; its ability needs 6 companions
					put("voice", "1_142");      // Traitor's Voice: Shadow support-area condition, not a weather
					put("urgency", "19_17");    // Urgency: Shadow weather condition in the support area
					put("frost", "1_135");      // Saruman's Frost: Shadow weather condition borne by a site; Hobbits there are strength -2
					put("evil", "1_283");       // You Bring Great Evil: Shadow condition borne by a companion; matters only at a sanctuary
					put("evil2", "1_283");
					put("weary", "4_212");      // Weary: Shadow condition borne by a companion or ally; matters only on assignment to a tracker
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion; matters only on assignment
					put("poison2", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, so that the maneuver phase happens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MakeLightofBurdensStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Make Light of Burdens
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Maneuver: Exert a Dwarf and hinder an item borne by that Dwarf to hinder a Shadow condition (if that condition is a weather or borne by a Dwarf, discard it instead).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("burdens");

		assertEquals("Make Light of Burdens", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingADwarfAndHinderingHisItemHindersAShadowConditionInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var axe = scn.GetFreepsCard("axe");
		var ring = scn.GetFreepsCard("ring");
		var endurance = scn.GetFreepsCard("endurance");
		var stairs = scn.GetFreepsCard("stairs");
		var greed = scn.GetShadowCard("greed");
		var voice = scn.GetShadowCard("voice");
		var urgency = scn.GetShadowCard("urgency");
		var frost = scn.GetShadowCard("frost");
		var evil = scn.GetShadowCard("evil");
		var weary = scn.GetShadowCard("weary");
		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(burdens, stairs);
		scn.MoveCompanionsToTable(gimli, guard, guard2, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Gimli bears a possession, an artifact, the Shadow player's possession and a condition.
		scn.AttachCardsTo(gimli, armor, ring, poison, endurance, evil);
		scn.AttachCardsTo(grimir, axe, weary);
		// Aragorn bears an item but is no Dwarf; the first Dwarf Guard bears one but is exhausted (vitality 2);
		// the second can exert but bears no item.
		scn.AttachCardsTo(aragorn, poison2);
		scn.AttachCardsTo(guard, armor2);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToSupportArea(greed, voice, urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AttachCardsTo(scn.GetCurrentSite(), frost);
		scn.HinderCard(voice);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(greed));
		assertTrue(scn.FreepsActionAvailable(burdens));

		scn.FreepsUseCardAction(burdens);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guard, guard2));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, ring, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(endurance, evil, axe, poison2, armor2));
		scn.FreepsChooseCard(armor);

		assertTrue(scn.IsHindered(armor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(greed, urgency, frost, evil, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(voice, stairs, burdens, endurance, poison, poison2, ring, troop));
		scn.FreepsChooseCard(greed);

		assertTrue(scn.IsHindered(greed));
		assertInZone(Zone.SUPPORT, greed);
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.IsHindered(poison));
		assertAttachedTo(armor, gimli);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWeatherConditionIsDiscardedInsteadWhereverItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var greed = scn.GetShadowCard("greed");
		var urgency = scn.GetShadowCard("urgency");
		var frost = scn.GetShadowCard("frost");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, ring);
		scn.MoveCardsToSupportArea(greed, urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		var site = scn.GetCurrentSite();
		scn.AttachCardsTo(site, frost);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(burdens);
		scn.FreepsChooseCard(armor);
		scn.FreepsChooseCard(urgency);

		// A weather in the support area.
		assertInZone(Zone.DISCARD, urgency);
		assertFalse(scn.IsHindered(urgency));
		assertFalse(scn.IsHindered(greed));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.ShadowPass();
		assertAttachedTo(frost, site);
		scn.FreepsUseCardAction(burdens);
		// The armor is hindered, so the Ring is his only item and is taken without asking.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frost, greed));
		scn.FreepsChooseCard(frost);

		// A weather borne by a site.
		assertTrue(scn.IsHindered(ring));
		assertInZone(Zone.DISCARD, frost);
		assertFalse(scn.IsHindered(greed));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AShadowConditionBorneByADwarfIsDiscardedInsteadAndOneBorneByAnotherCompanionIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var axe = scn.GetFreepsCard("axe");
		var evil = scn.GetShadowCard("evil");
		var evil2 = scn.GetShadowCard("evil2");
		var weary = scn.GetShadowCard("weary");
		var greed = scn.GetShadowCard("greed");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(burdens, greed);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, armor, ring, evil);
		scn.AttachCardsTo(grimir, axe, weary);
		scn.AttachCardsTo(aragorn, evil2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// On the Dwarf who exerted.
		scn.FreepsUseCardAction(burdens);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(armor);
		scn.FreepsChooseCard(evil);

		assertInZone(Zone.DISCARD, evil);
		assertFalse(scn.IsHindered(evil));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// On a Dwarf ally who did not exert.
		scn.ShadowPass();
		scn.FreepsUseCardAction(burdens);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(weary);

		assertTrue(scn.IsHindered(ring));
		assertInZone(Zone.DISCARD, weary);
		assertFalse(scn.IsHindered(weary));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// On a companion who is not a Dwarf: hindered, and left where it is.
		scn.ShadowPass();
		scn.FreepsUseCardAction(burdens);
		// Gimli's items are both hindered, so Grimir exerts and his axe is hindered without asking.
		assertTrue(scn.FreepsHasCardChoiceAvailable(evil2, greed));
		scn.FreepsChooseCard(evil2);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.IsHindered(axe));
		assertTrue(scn.IsHindered(evil2));
		assertAttachedTo(evil2, aragorn);
		assertFalse(scn.IsHindered(greed));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AShadowConditionBorneByAHinderedDwarfIsHinderedNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var evil = scn.GetShadowCard("evil");
		var greed = scn.GetShadowCard("greed");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(burdens, greed);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(guard, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(guard);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(burdens);
		assertTrue(scn.FreepsHasCardChoiceAvailable(evil, greed));
		scn.FreepsChooseCard(evil);

		assertTrue(scn.IsHindered(evil));
		assertAttachedTo(evil, guard);
		assertFalse(scn.IsHindered(greed));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedUntilADwarfWhoCanExertBearsAnItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var endurance = scn.GetFreepsCard("endurance");
		var poison = scn.GetShadowCard("poison");
		var greed = scn.GetShadowCard("greed");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToSupportArea(burdens, greed);
		scn.MoveCompanionsToTable(guard, guard2, aragorn);
		// An exhausted Dwarf with an item, a Dwarf who can exert bearing only a condition,
		// and a companion with an item who is not a Dwarf.
		scn.AttachCardsTo(guard, armor);
		scn.AddWoundsToChar(guard, 1);
		scn.AttachCardsTo(guard2, endurance);
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(burdens));

		scn.AttachCardsTo(guard2, armor2);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(burdens));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
