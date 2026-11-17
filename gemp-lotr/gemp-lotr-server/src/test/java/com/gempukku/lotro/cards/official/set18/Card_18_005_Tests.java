package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dagger", "18_5");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, and Arwen
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an [elven] companion who is not Arwen
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion with no text but a play requirement
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] ally
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of another culture

					// Plain hand weapons an Elf may bear.
					put("sword1", "4_64");      // Elven Sword
					put("sword2", "4_64");
					put("blade", "11_16");      // Blade of Lindon

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwensDaggerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Arwen's Dagger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [elven] companion.<br>Bearer may bear this hand weapon in addition to 1 other hand weapon.<br><b>Maneuver:</b> If bearer is Arwen, exert her to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dagger");

		assertEquals("Arwen's Dagger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElvenCompanionWhoMayThenBeGivenAnotherHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword1 = scn.GetFreepsCard("sword1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(dagger, sword1);

		scn.StartGame();

		assertInHand(dagger, sword1);
		assertTrue(scn.FreepsPlayAvailable(dagger));
		scn.FreepsPlayCard(dagger);

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);
		assertAttachedTo(dagger, haldir);

		// Played the other way round: a plain hand weapon may still go on the Dagger's bearer.
		assertTrue(scn.FreepsPlayAvailable(sword1));
		scn.FreepsPlayCard(sword1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen));
		scn.FreepsChooseCard(haldir);

		assertAttachedTo(sword1, haldir);
		assertAttachedTo(dagger, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBeBorneBesideOneOtherHandWeaponButNotBesideTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);
		scn.MoveAlliesToTable(celeborn);
		// Haldir and Arwen each bear one plain hand weapon; the Lorien Elf and Celeborn bear none.
		scn.AttachCardsTo(haldir, sword1);
		scn.AttachCardsTo(arwen, blade);
		scn.MoveCardsToHand(dagger, sword2);

		scn.StartGame();

		assertAttachedTo(sword1, haldir);
		assertAttachedTo(blade, arwen);
		assertInHand(dagger, sword2);

		// Each of them may take the Dagger beside his own hand weapon.
		scn.FreepsPlayCard(dagger);
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen, lorienelf));
		scn.FreepsChooseCard(haldir);
		assertAttachedTo(dagger, haldir);
		assertAttachedTo(sword1, haldir);

		// A second plain hand weapon goes on neither Arwen, who bears one already, nor Haldir, who bears the Dagger
		// and one other.
		assertTrue(scn.FreepsPlayAvailable(sword2));
		scn.FreepsPlayCard(sword2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorienelf, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, haldir));
		scn.FreepsChooseCard(lorienelf);

		assertAttachedTo(sword2, lorienelf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsArwenToDrawTwoCardsWhileSheBearsTheDagger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var blade = scn.GetFreepsCard("blade");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, dagger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// From the top: Elven Sword, Elven Sword, Blade of Lindon.
		scn.MoveCardsToTopOfDeck(blade, sword2, sword1);
		assertInZone(Zone.DECK, sword1, sword2, blade);
		assertSame(sword1, scn.GetFreepsTopOfDeck());
		assertSame(sword2, scn.GetFromTopOfFreepsDeck(2));
		assertSame(blade, scn.GetFromTopOfFreepsDeck(3));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(dagger, arwen);
		assertSame(sword1, scn.GetFreepsTopOfDeck());
		assertSame(sword2, scn.GetFromTopOfFreepsDeck(2));
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsActionAvailable(dagger));

		scn.FreepsUseCardAction(dagger);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(sword1, sword2);
		assertSame(blade, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheBearerIsNotArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(arwen, haldir);
		// Arwen is on the table and could exert, but Haldir bears the Dagger.
		scn.AttachCardsTo(haldir, dagger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(dagger, haldir);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dagger));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileArwenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, dagger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 3 with 2 wounds: Arwen cannot exert.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(dagger, arwen);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dagger));
	}
}
