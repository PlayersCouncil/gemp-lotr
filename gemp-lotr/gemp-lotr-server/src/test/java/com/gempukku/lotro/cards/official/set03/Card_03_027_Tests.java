package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vilya", "3_27");
					put("elrond", "12_17");     // Elrond, Witness to History: Elf companion, vitality 4; skirmish ability only
					put("elrondally", "1_40");  // Elrond, Lord of Rivendell: Elf ally, vitality 4; heals allies of site 3 at the start of your turn
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion; a response only for the Ring-bearer's wounds
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; text only at your site 3
					put("lordofmoria", "1_21"); // Lord of Moria: Free Peoples [dwarven] condition in the support area

					put("weary", "4_212");      // Weary: [isengard] condition borne by a companion; exerts only on assignment to a tracker
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition in the support area; Shadow ability only
					put("poison", "8_28");      // Spider Poison: Shadow possession in the support area
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Elrond bearing Vilya, a minion so that there is a maneuver phase, and a Shadow condition to put down later.
	protected VirtualTableScenario GetShadowConditionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vilya", "3_27");
					put("elrond", "1_40");
					put("fear", "1_278");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VilyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Vilya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Elrond.
		 * <b>Maneuver:</b> Exert Elrond twice and spot a Shadow condition to return that condition to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vilya");

		assertEquals("Vilya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsNotPlayableWithoutElrondAndPlaysOnAnElrondAllyAndOnNoOtherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrondally");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(vilya, elrond);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();

		// An Elf companion, an Elf ally and the Ring-bearer, and no Elrond: nobody may bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(vilya));

		// Elrond the ally is played (he needs an Elf spotted: Arwen).
		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.FreepsPlayAvailable(vilya));

		scn.FreepsPlayCard(vilya);
		// Elrond is the only bearer there is, beside an Elf companion and an Elf ally.
		assertAttachedTo(vilya, elrond);
		assertNotAttachedTo(vilya, arwen);
		assertNotAttachedTo(vilya, silinde);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAnElrondCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(vilya);
		// Elrond the companion on a board of his own: he is unique, so Elrond the ally is not in play beside him.
		scn.MoveCompanionsToTable(elrond, arwen);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(vilya));
		scn.FreepsPlayCard(vilya);
		// Elrond is the only bearer there is, beside an Elf companion and an Elf ally.
		assertAttachedTo(vilya, elrond);
		assertNotAttachedTo(vilya, arwen);
		assertNotAttachedTo(vilya, silinde);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverExertsElrondTwiceToReturnAShadowConditionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrondally");
		var arwen = scn.GetFreepsCard("arwen");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		// The Free Peoples player's own Shadow condition, inactive on his own turn (CR 4.2 "active").
		var ownseeking = scn.GetFreepsCard("seeking");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToSupportArea(lordofmoria, ownseeking);

		var weary = scn.GetShadowCard("weary");
		var seeking = scn.GetShadowCard("seeking");
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 4 + 1 with 2 wounds: 3 left, enough to exert twice.
		scn.AddWoundsToChar(elrond, 2);
		scn.AttachCardsTo(arwen, weary);
		scn.MoveCardsToSupportArea(seeking, poison);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(vilya));

		scn.FreepsUseCardAction(vilya);
		assertEquals(4, scn.GetWoundsOn(elrond));
		// Shadow conditions of two cultures, on a companion and in the support area; not a Shadow possession, a
		// Free Peoples condition, a minion, or a Shadow condition that is not active.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, seeking));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, lordofmoria, troop, ownseeking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		// Back in the Shadow player's hand; nothing else has moved.
		assertInZone(Zone.HAND, weary);
		assertEquals(1, scn.GetShadowHandCount());
		assertNotAttachedTo(weary, arwen);
		assertInZone(Zone.SUPPORT, seeking, poison, lordofmoria, ownseeking);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAFreePeoplesConditionAShadowPossessionAndAnInactiveShadowConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var ownseeking = scn.GetFreepsCard("seeking");
		scn.MoveCompanionsToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToSupportArea(lordofmoria, ownseeking);

		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToSupportArea(poison);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);

		// Elrond can exert twice; what there is to spot is a condition of the wrong side, a Shadow card that is
		// no condition, and the Free Peoples player's own Shadow condition, inactive on his turn.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));
	}

	@Test
	public void NotOfferedWhenElrondCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		// Vitality 4 + 1 with 3 wounds: 2 left, one short of exerting twice.
		scn.AddWoundsToChar(elrond, 3);

		var seeking = scn.GetShadowCard("seeking");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetVitality(elrond));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));

		scn.RemoveWoundsFromChar(elrond, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(vilya));
	}

	// "Exert Elrond twice and spot a Shadow condition to return that condition to its owner's hand": the
	// definition asked for no Shadow condition, and Elrond could exert twice for nothing.
	@Test
	public void TheAbilityWantsAShadowConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);

		var fear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Elrond could exert twice, and there is no Shadow condition.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));

		// Strength Born of Fear is put down in the Shadow player's support area.
		scn.MoveCardsToSupportArea(fear);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(vilya));
		scn.FreepsUseCardAction(vilya);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertInZone(Zone.HAND, fear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
