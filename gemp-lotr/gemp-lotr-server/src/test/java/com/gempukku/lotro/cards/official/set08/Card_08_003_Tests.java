package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chill", "8_3");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, damage +1, vitality 3; a skirmish ability only
					put("axe", "1_14");         // Gimli's Battle Axe: bearer must be Gimli, he is damage +1 (so +2); an optional trigger when he wins a skirmish
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3, no damage bonus of his own; a fellowship ability only
					put("ring", "9_8");         // Ring of Guile: artifact, bearer must be a Dwarf, bearer is damage +1; a maneuver ability used only on purpose
					put("huorn", "9_25");       // Huorn: a [gandalf] companion who is damage +1 and no Dwarf, vitality 4; a response that needs an Ent

					// Gimli, Counter of Foes: "Damage +1. For each wound on Gimli, he is strength +1 and damage +1."
					// Never on a table with the other Gimli.
					put("counter", "8_5");

					put("savage", "1_151");     // Uruk Savage: a minion; "Damage +1." is its whole text
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition in the support area; a skirmish ability only
					put("ships", "8_65");       // Ships of Great Draught: a [raider] possession in the support area; its abilities need a threat, a card in hand or tokens
					put("poison", "8_28");      // Spider Poison: a [gollum] possession a companion bears; it acts only when its bearer is assigned to a skirmish
					put("greed1", "1_125");     // Greed: an [isengard] condition in the Shadow support area, not unique
					put("greed2", "1_125");
					put("greed3", "1_125");
					put("greed4", "1_125");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a Dwarf who is damage +X and exert that Dwarf twice to make an opponent discard X Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chill");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoExertsTwiceAndTheShadowPlayerDiscardsTwoShadowCardsOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, huorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(chill);

		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		var ships = scn.GetShadowCard("ships");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(fear, ships);
		scn.AttachCardsTo(huorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// 3 companions + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(chill));
		scn.FreepsPlayCard(chill);

		// A Dwarf with a damage bonus, companion or ally; not Huorn, who has the bonus and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(huorn));

		// Gimli with his axe is damage +2: the Shadow player, and not the Free Peoples player, picks 2 Shadow
		// cards, of any type and wherever they lie.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, fear, ships, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, axe, grimir, ring, huorn));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(savage, poison);

		assertInDiscard(savage, poison);
		assertInZone(Zone.SUPPORT, fear, ships);
		assertInDiscard(chill);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnAllyWhoIsDamagePlusOneExertsTwiceAndTheShadowPlayerDiscardsOneShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(gimli, huorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(chill);

		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		var ships = scn.GetShadowCard("ships");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(fear, ships);
		scn.AttachCardsTo(huorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(chill);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsChooseCard(grimir);

		// X is the bonus of the Dwarf who exerted, not the greatest on the table.
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, fear, ships, poison));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(ships);

		assertInDiscard(ships);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, fear);
		assertAttachedTo(poison, huorn);
		assertInDiscard(chill);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoDwarfThoughAnotherCompanionHasADamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveCardsToHand(chill);

		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Huorn is damage +1 and could exert twice, but he is a Tree.
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertInHand(chill);
		assertFalse(scn.FreepsPlayAvailable(chill));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfWithADamageBonusCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var gimli = scn.GetFreepsCard("gimli");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(gimli, huorn);
		scn.MoveCardsToHand(chill);

		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		// Vitality 3 with 1 wound: Gimli can exert once more, not twice.
		scn.AddWoundsToChar(gimli, 1);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertInHand(chill);
		assertFalse(scn.FreepsPlayAvailable(chill));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithADwarfWhoHasNoDamageBonusUntilHeHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(chill);

		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Grimir is a Dwarf with the vitality to exert twice and no damage bonus; Huorn has the bonus and is no Dwarf.
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertInHand(chill);
		assertFalse(scn.FreepsPlayAvailable(chill));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The same Dwarf with a damage bonus of his own is the Dwarf the event wants.
		scn.AttachCardsTo(grimir, ring);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(chill));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFewerShadowCardsInPlayThanXEveryOneOfThemIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(chill);

		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, fear);
		scn.FreepsPlayCard(chill);

		// X is 2 and one Shadow card is in play: it goes, and that is all.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInDiscard(fear);
		assertInDiscard(chill);
		assertAttachedTo(axe, gimli);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void XIsTheDamageBonusTheDwarfHadWhenHeWasSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var counter = scn.GetFreepsCard("counter");
		scn.MoveCompanionsToTable(counter);
		scn.MoveCardsToHand(chill);

		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		var ships = scn.GetShadowCard("ships");
		var greed = scn.GetShadowCard("greed1");
		scn.MoveCardsToSupportArea(fear, ships, greed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// Unwounded, this Gimli is damage +1; each of the two exertions that pay for the event makes him +1 more.
		assertEquals(0, scn.GetWoundsOn(counter));
		scn.FreepsPlayCard(chill);
		assertEquals(2, scn.GetWoundsOn(counter));

		// He was damage +1 when he was spotted: 1 Shadow card, of the four.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, fear, ships, greed));
		scn.ShadowChooseCard(fear);

		assertInDiscard(fear);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, ships, greed);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AHinderedShadowCardMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");
		var counter = scn.GetFreepsCard("counter");
		scn.MoveCompanionsToTable(counter);
		scn.MoveCardsToHand(chill);

		var hindered1 = scn.GetShadowCard("greed1");
		var hindered2 = scn.GetShadowCard("greed2");
		var greed3 = scn.GetShadowCard("greed3");
		var greed4 = scn.GetShadowCard("greed4");
		scn.MoveCardsToSupportArea(hindered1, hindered2, greed3, greed4);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098).
		scn.HinderCard(hindered1, hindered2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(chill);

		// Gimli was damage +1 when he was spotted, whatever his two wounds make him since (Q-0256): X is 1, of
		// the four Shadow cards, hindered or not.
		assertEquals(2, scn.GetWoundsOn(counter));
		assertTrue(scn.ShadowHasCardChoiceAvailable(hindered1, hindered2, greed3, greed4));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hindered1);

		assertInDiscard(hindered1);
		assertInZone(Zone.SUPPORT, hindered2, greed3, greed4);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
