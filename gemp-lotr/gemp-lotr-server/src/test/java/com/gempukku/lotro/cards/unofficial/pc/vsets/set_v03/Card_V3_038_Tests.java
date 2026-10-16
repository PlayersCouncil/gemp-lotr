package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_038_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illturn", "103_38");
					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] Wizard minion
					put("follower", "17_38");   // Saruman, Servant of Sauron: [isengard] Shadow follower
					put("rabble", "4_33");      // Saruman, Rabble-rouser: [dunland] Wizard minion, cost 4, site 4; exerts companions on a move for each [dunland] Man
					put("traitor", "4_173");    // Saruman, Black Traitor: cost 4, site 4. When you play Saruman, you may discard a condition
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; adds (2) when played
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession borne by a [moria] Orc
					put("hollowing", "3_54");   // Hollowing of Isengard: [isengard] condition. Shadow: discard this condition to add (3)
					put("pinned", "1_192");     // Pinned Down: [moria] condition; does nothing without a [moria] archer
					put("norearly", "103_37");  // Nor is He Early: [isengard] condition whose response hinders itself
					put("whisper", "103_50");   // Desert Wind Whisper: [raider] minion, cost 5, site 4; when played, remove (4) or hinder it
					put("fury", "103_39");      // Savage Fury: Maneuver: discard a minion to make your [isengard] minion fierce or relentless

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Wizard, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: ranger, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf. Archery: exert Legolas to wound a minion
					put("merry", "1_302");      // Merry, Friend to Sam: cost 1
					put("gaffer", "1_291");     // The Gaffer: ally; a Free Peoples character who is no companion
					put("albert", "1_69");      // Albert Dreary: ally, vitality 3. Maneuver: exert him to discard an [isengard] or [moria] condition
					put("enter", "103_9");      // You Cannot Enter Here: Maneuver event. Exert your Wizard to hinder a Shadow card; exhaust that Wizard if it was a minion
					put("vengeance1", "1_106"); // Gondor's Vengeance: Regroup event. Exert a ranger companion to discard a minion
					put("vengeance2", "1_106");
					put("roll", "4_99");        // Roll of Thunder: Maneuver event. Spot Gandalf to discard a Shadow possession or artifact
					put("lordmoria", "1_21");   // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
					put("stranger", "1_108");   // No Stranger to the Shadows: Free Peoples condition borne by a ranger
					put("sword", "1_299");      // Hobbit Sword: Free Peoples possession borne by a Hobbit
					put("thereandback", "1_317"); // There and Back Again: Free Peoples condition borne by a Hobbit companion. Maneuver: discard this condition to make each Hobbit companion strength +2
					put("hospitality", "101_1");  // Hospitality of the Dwarves: Free Peoples condition in the support area. Maneuver: spot an unbound Dwarf bearing 2 items and hinder this condition to make that Dwarf damage +1 or hard to wound
					put("armor", "1_8");        // Dwarven Armor: possession borne by a Dwarf
					put("axe", "1_9");          // Dwarven Axe: possession borne by a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneIllTurnDeservesAnotherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: One Ill Turn Deserves Another
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [isengard] minions (or Saruman).
		 * 	Each time your minion is hindered or discarded by a Free Peoples card, hinder a companion.
		 * 	Each time your condition is hindered or discarded by a Free Peoples card, hinder a Free Peoples condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illturn");

		assertEquals("One Ill Turn Deserves Another", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneIsengardMinionUntilASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToHand(illturn, savage2);
		// An [isengard] card that is no minion.
		scn.MoveCardsToSupportArea(hollowing);

		scn.StartGame();
		// One [isengard] minion and a minion of another culture.
		scn.MoveMinionsToTable(savage1, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(savage2);
		// 13 - 2 to play - 2 roaming (a site 5 minion played at site 2)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(illturn);
		assertInZone(Zone.SUPPORT, illturn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SarumanTheMinionAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(illturn);
		assertInZone(Zone.SUPPORT, illturn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASarumanOfDunlandAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		// "Saruman" has no culture word: a [dunland] Saruman is spotted as an [isengard] one is.
		var rabble = scn.GetShadowCard("rabble");
		scn.MoveCardsToHand(illturn, rabble);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(rabble);
		// 13 - 4 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(illturn);
		assertInZone(Zone.SUPPORT, illturn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SarumanTheFollowerAloneIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var follower = scn.GetShadowCard("follower");
		scn.MoveCardsToHand(illturn);
		// No minion at all: the text asks for Saruman, of whatever card type.
		scn.MoveCardsToSupportArea(follower);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(illturn));

		scn.ShadowPlayCard(illturn);
		assertInZone(Zone.SUPPORT, illturn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMinionDiscardedByAFreePeoplesCardHindersACompanionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		var vengeance1 = scn.GetFreepsCard("vengeance1");
		var vengeance2 = scn.GetFreepsCard("vengeance2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.MoveCardsToHand(vengeance1, vengeance2);

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(vengeance1);
		scn.FreepsChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, savage1);

		// The Shadow player must hinder a companion: any of the three, but not the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gaffer));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gaffer));
		// A minion's discard is not answered on the Free Peoples conditions.
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();
		// A minion of another culture, discarded the same way: a second companion is hindered.
		scn.FreepsPlayCard(vengeance2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, runner);

		// Gimli, already hindered, is not offered again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, gaffer));

		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AMinionHinderedByAFreePeoplesCardHindersACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var enter = scn.GetFreepsCard("enter");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.MoveCardsToHand(enter);

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(runner));

		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(runner);
		assertTrue(scn.IsHindered(runner));
		assertFalse(scn.IsHindered(savage1));

		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gaffer));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(gaffer));
		assertFalse(scn.IsHindered(lordmoria));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachConditionDiscardedByAFreePeoplesCardHindersAFreePeoplesConditionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		// A Free Peoples condition in the support area, one on a companion, and a possession.
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.AttachCardsTo(frodo, sword);

		var illturn = scn.GetShadowCard("illturn");
		var hollowing = scn.GetShadowCard("hollowing");
		var pinned = scn.GetShadowCard("pinned");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(illturn, hollowing, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(albert));

		scn.FreepsUseCardAction(albert);
		scn.FreepsChooseCard(hollowing);
		assertEquals(1, scn.GetWoundsOn(albert));
		assertInZone(Zone.DISCARD, hollowing);

		// The Shadow player must hinder a Free Peoples condition, wherever it is; not a possession, and not
		// a condition of his own.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, pinned, illturn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(stranger);
		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(sword));
		// A condition's discard is not answered on the companions.
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// A condition of another culture, discarded the same way: the one Free Peoples condition left
		// unhindered is hindered.
		scn.FreepsUseCardAction(albert);
		scn.FreepsChooseCard(pinned);
		assertEquals(2, scn.GetWoundsOn(albert));
		assertInZone(Zone.DISCARD, pinned);
		assertTrue(scn.IsHindered(lordmoria));
		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionHinderedByAFreePeoplesCardHindersAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		var sword = scn.GetFreepsCard("sword");
		var enter = scn.GetFreepsCard("enter");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(enter);

		var illturn = scn.GetShadowCard("illturn");
		var pinned = scn.GetShadowCard("pinned");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(illturn, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(pinned);
		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(illturn));

		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, illturn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lordmoria);
		assertTrue(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FreePeoplesConditionsDiscardedOrHinderedByFreePeoplesCardsHinderNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var thereandback = scn.GetFreepsCard("thereandback");
		var hospitality = scn.GetFreepsCard("hospitality");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		// The 2 items Hospitality of the Dwarves asks its Dwarf to bear.
		scn.AttachCardsTo(gimli, armor, axe);
		scn.AttachCardsTo(frodo, thereandback);
		scn.MoveCardsToSupportArea(hospitality, lordmoria);

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		// A condition discarded by a Free Peoples card, but the Free Peoples player's own: There and Back
		// Again discards itself.
		assertAttachedTo(thereandback, frodo);
		scn.FreepsUseCardAction(thereandback);
		assertInZone(Zone.DISCARD, thereandback);
		assertFalse(scn.IsHindered(hospitality));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// A condition hindered by a Free Peoples card, again the Free Peoples player's own: Hospitality of
		// the Dwarves hinders itself.
		scn.FreepsUseCardAction(hospitality);
		scn.FreepsChoose("damage +1");
		assertTrue(scn.IsHindered(hospitality));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(illturn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ItsOwnHinderingByAFreePeoplesCardHindersOneFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		var enter = scn.GetFreepsCard("enter");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.MoveCardsToHand(enter);

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(illturn);
		assertTrue(scn.IsHindered(illturn));

		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lordmoria);
		// One condition for the one hindering: the other stays as it was, and so do the companions.
		assertTrue(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ItsOwnDiscardByAFreePeoplesCardHindersOneFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);

		var illturn = scn.GetShadowCard("illturn");
		var pinned = scn.GetShadowCard("pinned");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(illturn, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(albert);
		scn.FreepsChooseCard(illturn);
		assertInZone(Zone.DISCARD, illturn);

		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(stranger);
		// One condition for the one discard: the other stays as it was, and so do the companions.
		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ItsOwnDiscardByAShadowCardHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);

		var illturn = scn.GetShadowCard("illturn");
		var traitor = scn.GetShadowCard("traitor");
		scn.MoveCardsToSupportArea(illturn);
		scn.MoveCardsToHand(traitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for the shadow number of site 2, less 1 for No Stranger to the Shadows
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(traitor);
		// 13 - 4 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traitor));

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(illturn);
		assertInZone(Zone.DISCARD, illturn);
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowCardsHinderingOrDiscardingTheirOwnersMinionsAndConditionsHinderNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.MoveCardsToHand(merry);

		var illturn = scn.GetShadowCard("illturn");
		var norearly = scn.GetShadowCard("norearly");
		var hollowing = scn.GetShadowCard("hollowing");
		var fury = scn.GetShadowCard("fury");
		var whisper = scn.GetShadowCard("whisper");
		var savage1 = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(illturn, norearly, hollowing, fury);
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, runner);
		scn.FreepsRefreshDecision();
		scn.SetTwilight(20);

		// A condition hindered by a Shadow card: Nor is He Early answers a character played and hinders itself.
		scn.FreepsPlayCard(merry);
		scn.ShadowChooseOptionalTrigger(norearly);
		assertTrue(scn.IsHindered(norearly));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());

		// A minion hindered by a Shadow card: Desert Wind Whisper hinders itself as it is played.
		scn.ShadowPlayCard(whisper);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(whisper));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A condition discarded by a Shadow card: Hollowing of Isengard discards itself.
		scn.ShadowUseCardAction(hollowing);
		assertInZone(Zone.DISCARD, hollowing);
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.FreepsPass();

		// A minion discarded by a Shadow card: Savage Fury's cost.
		scn.ShadowUseCardAction(fury);
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		scn.ShadowChoose("Fierce");
		assertTrue(scn.HasKeyword(savage1, Keyword.FIERCE));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionDiscardedAndAMinionKilledByFreePeoplesCardsHinderNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var roll = scn.GetFreepsCard("roll");
		scn.MoveCompanionsToTable(gandalf, legolas);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.MoveCardsToHand(roll);

		var illturn = scn.GetShadowCard("illturn");
		var savage1 = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		// A Shadow card that is neither a minion nor a condition, discarded by a Free Peoples card.
		scn.FreepsPlayCard(roll);
		assertInZone(Zone.DISCARD, scimitar);
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(legolas));
		assertFalse(scn.IsHindered(lordmoria));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);

		// A minion killed, not discarded, by a Free Peoples card.
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(runner);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(legolas));
		assertFalse(scn.IsHindered(lordmoria));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
